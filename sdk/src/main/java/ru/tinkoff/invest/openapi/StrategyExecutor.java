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
    private boolean hasRun;
    private Logger logger;

    /**
     * Создаёт исполнителя заданной стратегии на заданном контексте. Процесс торговли при этом не запускается!
     *
     * @param context Контекст OpenAPI.
     * @param strategy Исполняемая стратегия.
     * @param logger Экзепляер логгера.
     */
    public StrategyExecutor(Context context, Strategy strategy, Logger logger) {
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
        strategy.getLimitOrderPublisher().subscribe(new LimitOrderSubscriber());
        strategy.getCancelPublisher().subscribe(new CancelSubscriber());

        final var figi = strategy.getInstrument().getFigi();
        context.sendStreamingRequest(
                StreamingRequest.subscribeInstrumentInfo(figi));
        context.sendStreamingRequest(
                StreamingRequest.subscribeOrderbook(figi, strategy.getOrderbookDepth()));
        context.sendStreamingRequest(
                StreamingRequest.subscribeCandle(figi, strategy.getCandleInterval()));

        hasRun = true;
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
                strategy.consumeCandle(candle);
            } else if (item instanceof StreamingEvent.Orderbook) {
                final var orderbook = (StreamingEvent.Orderbook)item;
                strategy.consumeOrderbook(orderbook);
            } else {
                final var instrumentInfo = (StreamingEvent.InstrumentInfo)item;
                strategy.consumeInstrumentInfo(instrumentInfo);
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

    private class LimitOrderSubscriber implements Flow.Subscriber<LimitOrder> {

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(LimitOrder item) {
            logger.info("Стратегия решила разместить " + item.getOperation() + " заявку на " + item.getLots() +
                    " лотов по цене " + item.getPrice() + " " + strategy.getInstrument().getCurrency());
            context.placeLimitOrder(item).thenApply(plo -> {
                strategy.consumePlacedLimitOrder(plo);
                return null;
            }).exceptionally(ex -> {
                logger.log(Level.WARNING, "Заявка не размещена.", ex);
                strategy.consumeRejectedLimitOrder(item.getOperation());
                return null;
            });
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(Level.SEVERE, "Что-то пошло не так в подписке на стрим LimitOrder.", throwable);
        }

        @Override
        public void onComplete() {
        }
    }

    private class CancelSubscriber implements Flow.Subscriber<String> {

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(String item) {
            context.cancelOrder(item);
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(Level.SEVERE, "Что-то пошло не так в подписке на стрим Cancel.", throwable);
        }

        @Override
        public void onComplete() {
        }
    }
}
