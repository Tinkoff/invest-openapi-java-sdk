package ru.tinkoff.invest.openapi.model.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Модель даных по историческим свечам.
 */
public class HistoricalCandles {

    /**
     * Идентификатор инструмента описываемого свечами.
     */
    public final String figi;

    /**
     * Интервал времени свечей.
     */
    public final CandleInterval interval;

    /**
     * Список свечей.
     */
    public final List<Candle> candles;

    @JsonCreator
    public HistoricalCandles(@JsonProperty("figi")
                             String figi,
                             @JsonProperty("interval")
                             CandleInterval interval,
                             @JsonProperty("candles")
                             List<Candle> candles) {
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
        }
        if (Objects.isNull(interval)) {
            throw new IllegalArgumentException("Интервал не может быть null.");
        }
        if (Objects.isNull(candles)) {
            throw new IllegalArgumentException("Список свечей не может быть null.");
        }

        this.figi = figi;
        this.interval = interval;
        this.candles = candles;
    }

}
