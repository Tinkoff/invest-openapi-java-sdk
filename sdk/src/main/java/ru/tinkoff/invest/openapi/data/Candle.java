package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Candle {
    private final String figi;
    private final CandleInterval interval;
    private final BigDecimal o;
    private final BigDecimal c;
    private final BigDecimal h;
    private final BigDecimal l;
    private final BigDecimal v;
    private final OffsetDateTime time;

    @JsonCreator
    public Candle(@JsonProperty("figi")
                  String figi,
                  @JsonProperty("interval")
                  CandleInterval interval,
                  @JsonProperty("o")
                  BigDecimal o,
                  @JsonProperty("c")
                  BigDecimal c,
                  @JsonProperty("h")
                  BigDecimal h,
                  @JsonProperty("l")
                  BigDecimal l,
                  @JsonProperty("v")
                  BigDecimal v,
                  @JsonProperty("time")
                  OffsetDateTime time) {
        this.figi = figi;
        this.interval = interval;
        this.o = o;
        this.c = c;
        this.h = h;
        this.l = l;
        this.v = v;
        this.time = time;
    }

    public String getFigi() {
        return figi;
    }

    public CandleInterval getInterval() {
        return interval;
    }

    public BigDecimal getO() {
        return o;
    }

    public BigDecimal getC() {
        return c;
    }

    public BigDecimal getH() {
        return h;
    }

    public BigDecimal getL() {
        return l;
    }

    public BigDecimal getV() {
        return v;
    }

    public OffsetDateTime getTime() {
        return time;
    }
}
