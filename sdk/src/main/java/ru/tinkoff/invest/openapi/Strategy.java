package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.*;

import java.math.BigDecimal;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Базовый класс стратегии торгового робота. Стратегия это чёрный ящщик, который на вход принимает исследуемый
 * инструмент и поток биржевых данных, а на выходе подаёт сигналы о размещении или отмене лимитных заявок.
 */
public abstract class Strategy implements Flow.Processor<TradingState, StrategyDecision> {

    protected final Instrument operatingInstrument;
    protected final BigDecimal maxOperationValue;
    protected final int maxOperationOrderbookDepth;
    protected final CandleInterval candlesOperationInterval;
    protected final Logger logger;
    private final SubmissionPublisher<StrategyDecision> streaming;

    protected Strategy(final Instrument operatingInstrument,
             final BigDecimal maxOperationValue,
             final int maxOperationOrderbookDepth,
             final CandleInterval candlesOperationInterval,
             final Logger logger) {
        if (!(maxOperationValue.compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("maxOperationValue должно быть положительным");
        }
        if (maxOperationOrderbookDepth <= 0) {
            throw new IllegalArgumentException("maxOperationOrderbookDepth должно быть положительным");
        }

        this.operatingInstrument = operatingInstrument;
        this.maxOperationValue = maxOperationValue;
        this.maxOperationOrderbookDepth = maxOperationOrderbookDepth;
        this.candlesOperationInterval = candlesOperationInterval;
        this.logger = logger;
        this.streaming = new SubmissionPublisher<>();
    }

    /**
     * Получение исследуемого инструмента.
     *
     * @return Инструмент.
     */
    public Instrument getInstrument() {
        return this.operatingInstrument;
    }

    /**
     * Получение разрешения, с которым принимаются потоковые данные с биржи.
     *
     * @return Разрешение.
     */
    public CandleInterval getCandleInterval() {
        return this.candlesOperationInterval;
    }

    /**
     * Максимальная глубина заявочного стакана, в которую смотрит стратегия.
     *
     * @return Глубина стакана.
     */
    public int getOrderbookDepth() {
        return this.maxOperationOrderbookDepth;
    }

    /**
     * Выполнение подготовительных действий.
     */
    abstract void init();

    /**
     * Выполнение завершающих действий (при остановке работы).
     */
    public void cleanup() {
        this.streaming.close();
    }

    protected abstract StrategyDecision reactOnMarketChange(final TradingState tradingState);

    @Override
    public void subscribe(Flow.Subscriber<? super StrategyDecision> subscriber) {
        streaming.subscribe(subscriber);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(TradingState item) {
        this.streaming.submit(reactOnMarketChange(item));
    }

    @Override
    public void onError(Throwable throwable) {
        logger.log(
                Level.SEVERE,
                "Что-то пошло не так в подписке на стрим TradingState.",
                throwable
        );
    }

    @Override
    public void onComplete() {
    }

}
