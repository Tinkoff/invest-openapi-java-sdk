package ru.tinkoff.invest.openapi.model.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tinkoff.invest.openapi.model.Currency;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель биржевого инструмента.
 */
public class Instrument {

    /**
     * Идентификатор инструмента.
     */
    public final String figi;

    /**
     * Краткий биржевой идентификатор ("тикер").
     */
    public final String ticker;

    /**
     * Международный идентификационный код ценной бумаги.
     * Может быть null.
     */
    public final String isin;

    /**
     * Минимальный шаг цены.
     * Может быть null.
     */
    public final BigDecimal minPriceIncrement;

    /**
     * Размер лота.
     */
    public final Integer lot;

    /**
     * Валюта цены инструмента.
     * Может быть null.
     */
    public final Currency currency;

    /**
     * Название компании-эмитента
     */
    public final String name;

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
                              Integer lot,
                      @JsonProperty("currency")
                              Currency currency,
                      @JsonProperty("name")
                              String name) {
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор не может быть null.");
        }
        if (Objects.isNull(ticker)) {
            throw new IllegalArgumentException("Тикер не может быть null.");
        }
        if (Objects.isNull(lot)) {
            throw new IllegalArgumentException("Размер лота не может быть null.");
        }
        if (Objects.isNull(name)) {
            throw new IllegalArgumentException("Имя эмитента не может быть null.");
        }

        this.figi = figi;
        this.ticker = ticker;
        this.isin = isin;
        this.minPriceIncrement = minPriceIncrement;
        this.lot = lot;
        this.currency = currency;
        this.name = name;
    }
}
