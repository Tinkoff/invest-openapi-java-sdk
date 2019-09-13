package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.StreamingEvent;

/**
 * Объект представляющий для стратегии информацию о ситуации на рынке.
 */
public class TradingState {
    private StreamingEvent.Orderbook orderbook;
    private StreamingEvent.Candle candle;
    private StreamingEvent.InstrumentInfo instrumentInfo;

    /**
     * Создаёт новое состояние из "стакана", "свечи" и информации по рассматриваемому инструменту.
     *
     * Все входные данные допускают содержать null.
     *
     * @param orderbook Состояние "стакана" (книги заявок).
     * @param candle Ценовая "свеча".
     * @param instrumentInfo Информация по инструменту.
     */
    public TradingState(StreamingEvent.Orderbook orderbook,
                        StreamingEvent.Candle candle,
                        StreamingEvent.InstrumentInfo instrumentInfo) {
        this.orderbook = orderbook;
        this.candle = candle;
        this.instrumentInfo = instrumentInfo;
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
     * Установка нового значения "стакана".
     *
     * @param orderbook Новое значение "стакана".
     */
    public void setOrderbook(StreamingEvent.Orderbook orderbook) {
        this.orderbook = orderbook;
    }

    /**
     * Установка нового значения "свечи".
     *
     * @param candle Новое значение "стакана".
     */
    public void setCandle(StreamingEvent.Candle candle) {
        this.candle = candle;
    }

    /**
     * Установка нового значения информации по инструменту.
     *
     * @param instrumentInfo Новое значение информации по инструменту.
     */
    public void setInstrumentInfo(StreamingEvent.InstrumentInfo instrumentInfo) {
        this.instrumentInfo = instrumentInfo;
    }
}
