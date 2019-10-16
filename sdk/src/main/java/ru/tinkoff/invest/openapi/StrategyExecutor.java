package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.*;
import ru.tinkoff.invest.openapi.wrapper.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Исполнитель стратегии. Берёт торговую стратегию, контекст OpenAPI и осущствляет процесс автоматической торговли.
 */
public class StrategyExecutor {

    private final Context context;
    private final Strategy strategy;
    private final Logger logger;
    private boolean hasRun;
    private final Map<String, Runnable> orderTrackers;
    private SubmissionPublisher<TradingState> streaming;

    /**
     * Создаёт исполнителя заданной стратегии на заданном контексте. Процесс торговли при этом не запускается!
     *
     * @param context Контекст OpenAPI.
     * @param strategy Исполняемая стратегия.
     * @param logger Экзепляер логгера.
     */
    public StrategyExecutor(final Context context, final Strategy strategy, final Logger logger) {
        this.context = context;
        this.strategy = strategy;
        this.hasRun = false;
        this.logger = logger;
        this.orderTrackers = new HashMap<>();
        this.streaming = new SubmissionPublisher<>();
    }

    /**
     * Возвращает признак того, запущена ли стратегия.
     */
    public boolean isRunning() {
        return hasRun;
    }

    /**
     * Запускает процесс торговли. Если запуск уже был произведён, то ничего не происходит.
     *
     * Вызывает {@link Strategy#init}.
     */
    public void run() {
        if (hasRun) return;

        strategy.init();

        context.subscribe(new ContextSubscriber());
        strategy.subscribe(new StrategyDecisionSubscriber());
        streaming.subscribe(strategy);

        final var figi = strategy.getInstrument().getFigi();
        context.sendStreamingRequest(
                StreamingRequest.subscribeInstrumentInfo(figi));
        context.sendStreamingRequest(
                StreamingRequest.subscribeOrderbook(figi, strategy.getOrderbookDepth()));
        context.sendStreamingRequest(
                StreamingRequest.subscribeCandle(figi, strategy.getCandleInterval()));

        hasRun = true;
    }

    /**
     * Останавливает процесс торговли. Если остановка уже была произведена, то ничего не происходит.
     */
    public void stop() {
        if (!hasRun) return;

        final var figi = strategy.getInstrument().getFigi();
        context.sendStreamingRequest(
                StreamingRequest.unsubscribeInstrumentInfo(figi));
        context.sendStreamingRequest(
                StreamingRequest.unsubscribeCandle(figi, strategy.getCandleInterval()));
        context.sendStreamingRequest(
                StreamingRequest.unsubscribeOrderbook(figi, strategy.getOrderbookDepth()));

        context.unsubscribe();

        streaming.close();
        strategy.cleanup();

        hasRun = false;
    }

    private class ContextSubscriber implements Flow.Subscriber<StreamingEvent> {

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(StreamingEvent item) {
            if (item instanceof StreamingEvent.Candle) {
                final var candle = (StreamingEvent.Candle)item;
                streaming.submit(strategy.getCurrentState().copy(candle));
            } else if (item instanceof StreamingEvent.Orderbook) {
                final var orderbook = (StreamingEvent.Orderbook)item;
                streaming.submit(strategy.getCurrentState().copy(orderbook));
            } else if (item instanceof StreamingEvent.InstrumentInfo) {
                final var instrumentInfo = (StreamingEvent.InstrumentInfo)item;
                streaming.submit(strategy.getCurrentState().copy(instrumentInfo));
            } else {
                logger.severe("Что-то пошло не так в подписке на стрим StreamingEvent. " + item);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(Level.SEVERE, "Что-то пошло не так в подписке на стрим StreamingEvent.", throwable);
        }

        @Override
        public void onComplete() {
        }
    }

    private class StrategyDecisionSubscriber implements Flow.Subscriber<StrategyDecision> {

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(StrategyDecision item) {
            if (item instanceof StrategyDecision.PlaceLimitOrder) {
                final LimitOrder limitOrder = ((StrategyDecision.PlaceLimitOrder) item).getLimitOrder();

                logger.info("Стратегия решила разместить " + limitOrder.getOperation() + " заявку на " +
                        limitOrder.getLots() + " лотов по цене " + limitOrder.getPrice() + " " +
                        strategy.getInstrument().getCurrency());
                context.placeLimitOrder(limitOrder).thenApply(plo -> {
                    logger.fine("Заявка успешно размещена.");
                    final var orderStatus = plo.getOperation() == OperationType.Buy
                            ? TradingState.OrderStatus.WaitingBuy
                            : TradingState.OrderStatus.WaitingSell;
                    streaming.submit(strategy.getCurrentState().copy(orderStatus));
                    orderTrackers.put(plo.getId(), new OrderTracker(plo));
                    orderTrackers.get(plo.getId()).run();
                    return null;
                }).exceptionally(ex -> {
                    logger.log(Level.WARNING, "Заявка не размещена.", ex);
                    return null;
                });
            } else if (item instanceof StrategyDecision.CancelOrder) {
                final String orderId = ((StrategyDecision.CancelOrder) item).getOrderId();
                context.cancelOrder(orderId).thenApply(plo -> {
                    logger.fine("Заявка успешно отменена.");
                    streaming.submit(strategy.getCurrentState().copy(TradingState.OrderStatus.None));
                    return null;
                }).exceptionally(ex -> {
                    logger.log(Level.WARNING, "Заявка не отменена.", ex);
                    return null;
                });
            }
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(Level.SEVERE, "Что-то пошло не так в подписке на стрим StreamingEvent.", throwable);
        }

        @Override
        public void onComplete() {
        }
    }

    private class OrderTracker implements Runnable {

        private final PlacedLimitOrder order;
        private boolean done;
        private final Random randomEngine;
        private static final double rangeMin = 0.95;
        private static final double rangeMax = 1.05;

        OrderTracker(PlacedLimitOrder order) {
            this.order = order;
            this.done = false;
            this.randomEngine = new Random();
        }

        @Override
        public void run() {
            do {
                final var orders = context.getOrders().join();
                final var isOrderNotActive = orders.stream().noneMatch(o -> o.getId().equals(order.getId()));
                if (isOrderNotActive && strategy.getCurrentState().getInstrumentInfo().canTrade()) {
                    done = true;
                    final var newState = strategy.getCurrentState().copy(TradingState.OrderStatus.None).copy(
                            order.getOperation() == OperationType.Buy
                                    ? TradingState.PositionStatus.Exists
                                    : TradingState.PositionStatus.None
                    );
                    streaming.submit(newState);
                    orderTrackers.remove(order.getId());
                } else {
                    // получить число между rangeMin и rangeMax
                    var randomFactor = rangeMin + (rangeMax - rangeMin) * randomEngine.nextDouble();
                    var timeToSleep = Math.round(100*randomFactor);

                    try {
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException ignored) {
                    }
                }
            } while (!done);
        }
    }

}
