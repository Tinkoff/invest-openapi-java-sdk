package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.Currency;

import java.math.BigDecimal;

/**
 * Модель биржевого инструмента.
 */
public class Instrument {

    /**
     * Идентификатор инструмента.
     */
    @NotNull
    public final String figi;

    /**
     * Краткий биржевой идентификатор ("тикер").
     */
    @NotNull
    public final String ticker;

    /**
     * Международный идентификационный код ценной бумаги.
     */
    @Nullable
    public final String isin;

    /**
     * Минимальный шаг цены.
     */
    @Nullable
    public final BigDecimal minPriceIncrement;

    /**
     * Размер лота.
     */
    public final int lot;

    /**
     * Валюта цены инструмента.
     */
    @Nullable
    public final Currency currency;

    /**
     * Название компании-эмитента
     */
    @NotNull
    public final String name;

    @NotNull
    public final InstrumentType type;

    @JsonCreator
    public Instrument(@JsonProperty(value = "figi", required = true)
                      @NotNull
                      final String figi,
                      @JsonProperty(value = "ticker", required = true)
                      @NotNull
                      final String ticker,
                      @JsonProperty("isin")
                      @Nullable
                      final String isin,
                      @JsonProperty("minPriceIncrement")
                      @Nullable
                      final BigDecimal minPriceIncrement,
                      @JsonProperty(value = "lot", required = true)
                      final int lot,
                      @JsonProperty("currency")
                      @Nullable
                      final Currency currency,
                      @JsonProperty(value = "name", required = true)
                      @NotNull
                      final String name,
                      @JsonProperty(value = "type", required = true)
                      @NotNull
                      final InstrumentType type) {
        this.figi = figi;
        this.ticker = ticker;
        this.isin = isin;
        this.minPriceIncrement = minPriceIncrement;
        this.lot = lot;
        this.currency = currency;
        this.name = name;
        this.type = type;
    }
}
