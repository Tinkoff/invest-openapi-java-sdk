package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.StreamingEvent;

/**
 * Объект представляющий для стратегии информацию о ситуации на рынке.
 */
public class TradingState {
    private final StreamingEvent.Orderbook orderbook;
    private final StreamingEvent.Candle candle;
    private final StreamingEvent.InstrumentInfo instrumentInfo;
    private final PositionStatus positionStatus;
    private final OrderStatus orderStatus;

    /**
     * Создаёт новое состояние из "стакана", "свечи" и информации по рассматриваемому инструменту.
     *
     * @param orderbook Состояние "стакана" (книги заявок). Может быть null.
     * @param candle Ценовая "свеча". Может быть null.
     * @param instrumentInfo Информация по инструменту. Может быть null.
     * @param positionStatus Статус позиции по инструменту.
     * @param orderStatus Статус заявки по инструменту.
     */
    public TradingState(final StreamingEvent.Orderbook orderbook,
                        final StreamingEvent.Candle candle,
                        final StreamingEvent.InstrumentInfo instrumentInfo,
                        final PositionStatus positionStatus,
                        final OrderStatus orderStatus) {
        this.orderbook = orderbook;
        this.candle = candle;
        this.instrumentInfo = instrumentInfo;
        this.positionStatus = positionStatus;
        this.orderStatus = orderStatus;
    }

    /**
     * Получение "стакана".
     * Может вернуть null.
     */
    public StreamingEvent.Orderbook getOrderbook() {
        return orderbook;
    }

    /**
     * Получение "свечи".
     * Может вернуть null.
     */
    public StreamingEvent.Candle getCandle() {
        return candle;
    }

    /**
     * Получение информации по инструменту.
     * Может вернуть null.
     */
    public StreamingEvent.InstrumentInfo getInstrumentInfo() {
        return instrumentInfo;
    }

    /**
     * Получение статуса позиции.
     */
    public PositionStatus getPositionStatus() {
        return positionStatus;
    }

    /**
     * Получение статуса заявки.
     */
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public TradingState copy(final StreamingEvent.Orderbook orderbook) {
        return new TradingState(orderbook, this.candle, this.instrumentInfo, this.positionStatus, this.orderStatus);
    }

    public TradingState copy(final StreamingEvent.Candle candle) {
        return new TradingState(this.orderbook, candle, this.instrumentInfo, this.positionStatus, this.orderStatus);
    }

    public TradingState copy(final StreamingEvent.InstrumentInfo instrumentInfo) {
        return new TradingState(this.orderbook, this.candle, instrumentInfo, this.positionStatus, this.orderStatus);
    }

    public TradingState copy(final PositionStatus positionStatus) {
        return new TradingState(this.orderbook, this.candle, this.instrumentInfo, positionStatus, this.orderStatus);
    }

    public TradingState copy(OrderStatus orderStatus) {
        return new TradingState(this.orderbook, this.candle, this.instrumentInfo, this.positionStatus, orderStatus);
    }

    public enum PositionStatus { Exists, None }
    public enum OrderStatus { WaitingBuy, WaitingSell, None }
}
