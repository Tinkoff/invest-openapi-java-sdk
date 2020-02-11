package ru.tinkoff.invest.openapi.models.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечисление возможных типов инструментов.
 */
public enum InstrumentType {

    /**
     * Акция.
     */
    @JsonProperty("Stock")
    Stock,

    /**
     * Валюта.
     */
    @JsonProperty("Currency")
    Currency,

    /**
     * Облигация.
     */
    @JsonProperty("Bond")
    Bond,

    /**
     * Фонд.
     */
    @JsonProperty("Etf")
    Etf

}
