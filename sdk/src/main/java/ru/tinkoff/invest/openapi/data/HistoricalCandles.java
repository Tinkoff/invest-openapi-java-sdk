package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class HistoricalCandles {
    private final String figi;
    private final CandleInterval interval;
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
