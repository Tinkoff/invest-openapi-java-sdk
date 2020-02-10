package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Возможные типы инструментов.
 */
public enum InstrumentType {
    @JsonProperty("Stock")
    Stock,
    @JsonProperty("Currency")
    Currency,
    @JsonProperty("Bond")
    Bond,
    @JsonProperty("Etf")
    Etf
}
