package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Модель даных по историческим свечам.
 */
public final class HistoricalCandles {

    /**
     * Идентификатор инструмента описываемого свечами.
     */
    @NotNull
    public final String figi;

    /**
     * Интервал времени свечей.
     */
    @NotNull
    public final CandleInterval interval;

    /**
     * Список свечей.
     */
    @NotNull
    public final List<Candle> candles;

    @JsonCreator
    public HistoricalCandles(@JsonProperty(value = "figi", required = true)
                             @NotNull
                             final String figi,
                             @JsonProperty(value = "interval", required = true)
                             @NotNull
                             final CandleInterval interval,
                             @JsonProperty(value = "candles", required = true)
                             @NotNull
                             final List<Candle> candles) {
        this.figi = figi;
        this.interval = interval;
        this.candles = candles;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HistoricalCandles(");
        sb.append("figi='").append(figi).append('\'');
        sb.append(", interval=").append(interval);
        sb.append(", candles=").append(candles);
        sb.append(')');
        return sb.toString();
    }

}
