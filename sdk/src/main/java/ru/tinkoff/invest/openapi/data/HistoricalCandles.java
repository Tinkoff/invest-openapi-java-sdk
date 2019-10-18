package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Модель даных по историческим свечам возвращаемая OpenAPI.
 */
public class HistoricalCandles {

    /**
     * Идентификатор инструмента описываемого свечами.
     */
    private final String figi;

    /**
     * Интервал времени свечей.
     */
    private final CandleInterval interval;

    /**
     * Список свечей.
     */
    private final List<Candle> candles;

    @JsonCreator
    public HistoricalCandles(@JsonProperty("figi")
                             String figi,
                             @JsonProperty("interval")
                             CandleInterval interval,
                             @JsonProperty("candles")
                             List<Candle> candles) {
        this.figi = figi;
        this.interval = interval;
        this.candles = candles;
    }

    public String getFigi() {
        return figi;
    }

    public CandleInterval getInterval() {
        return interval;
    }

    public List<Candle> getCandles() {
        return candles;
    }
}
