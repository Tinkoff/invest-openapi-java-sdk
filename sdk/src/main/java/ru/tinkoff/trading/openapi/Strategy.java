package ru.tinkoff.trading.openapi;

import ru.tinkoff.trading.openapi.data.*;

import java.util.concurrent.Flow;

/**
 * Интерфейс стратегии торгового робота. Стратегия это чёрный ящщик, который на вход принимает исследуемый инструмент
 * и поток биржевых данных, а на выходе подаёт сигналы о размещении или отмене лимитных заявок.
 */
public interface Strategy {

    /**
     * Получение исследуемого инструмента.
     *
     * @return Инструмент.
     */
    Instrument getInstrument();

    /**
     * Получение разрешения, с которым принимаются потоковые данные с биржи.
     *
     * @return Разрешение.
     */
    CandleInterval getCandleInterval();

    /**
     * Максимальная глубина заявочного стакана, в которую смотрит стратегия.
     *
     * @return Глубина стакана.
     */
    int getOrderbookDepth();

    /**
     * Обработка очередных данных "свечи".
     *
     * @param candle Данные "свечи".
     */
    void consumeCandle(StreamingEvent.Candle candle);

    /**
     * Обработка очередных данных биржевого стакана.
     *
     * @param orderbook Состояние биржевого стакана.
     */
    void consumeOrderbook(StreamingEvent.Orderbook orderbook);

    /**
     * Обработка очередных данных по инструменту.
     *
     * @param instrumentInfo Данные по инструменту.
     */
    void consumeInstrumentInfo(StreamingEvent.InstrumentInfo instrumentInfo);

    /**
     * Поток событий на размещение лимитной заявки.
     *
     * @return Поток событий с заявками.
     */
    Flow.Publisher<LimitOrder> getLimitOrderPublisher();

    /**
     * Поток событий на отмену лимитной заявки.
     *
     * @return Поток событый с идентификаторами заявок.
     */
    Flow.Publisher<String> getCancelPublisher();

    /**
     * Обработка размещённой заявки.
     *
     * @param placedLimitOrder Размещённая заяка.
     */
    void consumePlacedLimitOrder(PlacedLimitOrder placedLimitOrder);

    /**
     * Обработка неразмещённой заявки.
     *
     * @param operationType Тип заявки.
     */
    void consumeRejectedLimitOrder(OperationType operationType);

    /**
     * Выполнение подготовительных действий.
     */
    void init();

}
