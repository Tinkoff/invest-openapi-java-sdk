package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Модель биржевого инструмента.
 */
public class Instrument {

    /**
     * Идентификатор инструмента.
     */
    private final String figi;

    /**
     * Краткий биржевой идентификатор ("тикер").
     */
    private final String ticker;

    /**
     * Международный идентификационный код ценной бумаги.
     */
    private final String isin;

    /**
     * Минимальный шаг цены.
     */
    private final BigDecimal minPriceIncrement;

    /**
     * Размер лота.
     */
    private final int lot;

    /**
     * Валюта цены инструмента.
     */
    private final Currency currency;

    /**
     * Название компании-эмитента
     */
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
