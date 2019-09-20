package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.LimitOrder;
import ru.tinkoff.invest.openapi.data.StreamingEvent;
import ru.tinkoff.invest.openapi.data.StreamingRequest;
import ru.tinkoff.invest.openapi.wrapper.Context;

import java.util.concurrent.Flow;
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
    private volatile TradingState currentState;

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

        final var figi = strategy.getInstrument().getFigi();
        context.sendStreamingRequest(
                StreamingRequest.subscribeInstrumentInfo(figi));
        context.sendStreamingRequest(
                StreamingRequest.subscribeOrderbook(figi, strategy.getOrderbookDepth()));
        context.sendStreamingRequest(
                StreamingRequest.subscribeCandle(figi, strategy.getCandleInterval()));

        hasRun = true;
        currentState = new TradingState(null, null ,null);
    }

    private class ContextSubscriber implements Flow.Subscriber<StreamingEvent> {

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(StreamingEvent item) {
            StrategyDecision strategyDecision;

            if (item instanceof StreamingEvent.Candle) {
                final var candle = (StreamingEvent.Candle)item;
                currentState.setCandle(candle);
                strategyDecision = strategy.reactOnMarketChange(currentState);
            } else if (item instanceof StreamingEvent.Orderbook) {
                final var orderbook = (StreamingEvent.Orderbook)item;
                currentState.setOrderbook(orderbook);
                strategyDecision = strategy.reactOnMarketChange(currentState);
            } else {
                final var instrumentInfo = (StreamingEvent.InstrumentInfo)item;
                currentState.setInstrumentInfo(instrumentInfo);
                strategyDecision = strategy.reactOnMarketChange(currentState);
            }

            if (strategyDecision instanceof StrategyDecision.PlaceLimitOrder) {
                final LimitOrder limitOrder = ((StrategyDecision.PlaceLimitOrder) strategyDecision).getLimitOrder();

                logger.info("Стратегия решила разместить " + limitOrder.getOperation() + " заявку на " +
                        limitOrder.getLots() + " лотов по цене " + limitOrder.getPrice() + " " +
                        strategy.getInstrument().getCurrency());
                context.placeLimitOrder(limitOrder).thenApply(plo -> {
                    logger.fine("Заявка успешно размещена.");
                    strategy.consumePlacedLimitOrder(plo);
                    return null;
                }).exceptionally(ex -> {
                    logger.log(Level.WARNING, "Заявка не размещена.", ex);
                    strategy.consumeRejectedLimitOrder(limitOrder);
                    return null;
                });
            } else if (strategyDecision instanceof StrategyDecision.CancelOrder) {
                final String orderId = ((StrategyDecision.CancelOrder) strategyDecision).getOrderId();
                context.cancelOrder(orderId).thenApply(plo -> {
                    logger.fine("Заявка успешно отменена.");

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

}
