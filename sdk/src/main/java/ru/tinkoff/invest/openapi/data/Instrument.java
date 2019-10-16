package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Instrument {
    private final String figi;
    private final String ticker;
    private final String isin;
    private final BigDecimal minPriceIncrement;
    private final int lot;
    private final Currency currency;
    private final String name;

    @JsonCreator
    public Instrument(@JsonProperty("figi")
                      String figi,
                      @JsonProperty("ticker")
                      String ticker,
                      @JsonProperty("isin")
                      String isin,
                      @JsonProperty("minPriceIncrement")
                      BigDecimal minPriceIncrement,
                      @JsonProperty("lot")
                      int lot,
                      @JsonProperty("currency")
                      Currency currency,
                      @JsonProperty("name")
                      String name) {
        this.figi = figi;
        this.ticker = ticker;
        this.isin = isin;
        this.minPriceIncrement = minPriceIncrement;
        this.lot = lot;
        this.currency = currency;
        this.name = name;
    }

    public String getFigi() {
        return figi;
    }

    public String getTicker() {
        return ticker;
    }

    public String getIsin() {
        return isin;
    }

    public BigDecimal getMinPriceIncrement() {
        return minPriceIncrement;
    }

    public int getLot() {
        return lot;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }
}
