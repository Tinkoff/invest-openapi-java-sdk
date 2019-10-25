package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.LimitOrder;
import ru.tinkoff.invest.openapi.data.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.data.StreamingEvent;

import java.math.BigDecimal;

/**
 * Объект представляющий для стратегии информацию о ситуации на рынке.
 */
public class TradingState {
    private final StreamingEvent.Orderbook orderbook;
    private final StreamingEvent.Candle candle;
    private final StreamingEvent.InstrumentInfo instrumentInfo;
    private final PositionInfo positionInfo;
    private final PlacedLimitOrder placedLimitOrder;

    /**
     * Создаёт новое состояние из "стакана", "свечи" и информации по рассматриваемому инструменту.
     *
     * @param orderbook Состояние "стакана" (книги заявок). Может быть null.
     * @param candle Ценовая "свеча". Может быть null.
     * @param instrumentInfo Информация по инструменту. Может быть null.
     * @param positionInfo Информация о позиции по инструменту. Может быть null.
     * @param placedLimitOrder Инофрмация о размещённой заявке. Может быть null.
     */
    public TradingState(final StreamingEvent.Orderbook orderbook,
                        final StreamingEvent.Candle candle,
                        final StreamingEvent.InstrumentInfo instrumentInfo,
                        final PositionInfo positionInfo,
                        final PlacedLimitOrder placedLimitOrder) {
        this.orderbook = orderbook;
        this.candle = candle;
        this.instrumentInfo = instrumentInfo;
        this.positionInfo = positionInfo;
        this.placedLimitOrder = placedLimitOrder;
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
     * Может вернуть null.
     */
    public PositionInfo getPositionInfo() {
        return positionInfo;
    }

    /**
     * Получение размещённой заявки.
     * Может вернуть null.
     */
    public PlacedLimitOrder getPlacedLimitOrder() {
        return placedLimitOrder;
    }


    public TradingState copy(final StreamingEvent.Orderbook orderbook) {
        return new TradingState(orderbook, this.candle, this.instrumentInfo, this.positionInfo, this.placedLimitOrder);
    }

    public TradingState copy(final StreamingEvent.Candle candle) {
        return new TradingState(this.orderbook, candle, this.instrumentInfo, this.positionInfo, this.placedLimitOrder);
    }

    public TradingState copy(final StreamingEvent.InstrumentInfo instrumentInfo) {
        return new TradingState(this.orderbook, this.candle, instrumentInfo, this.positionInfo, this.placedLimitOrder);
    }

    public TradingState copy(final PositionInfo positionInfo) {
        return new TradingState(this.orderbook, this.candle, this.instrumentInfo, positionInfo, this.placedLimitOrder);
    }

    public TradingState copy(final PlacedLimitOrder placedLimitOrder) {
        return new TradingState(this.orderbook, this.candle, this.instrumentInfo, this.positionInfo, placedLimitOrder);
    }

    public class PositionInfo {
        private final BigDecimal enterPrice;

        public PositionInfo(final BigDecimal enterPrice) {
            this.enterPrice = enterPrice;
        }

        public BigDecimal getEnterPrice() {
            return enterPrice;
        }
    }
}
