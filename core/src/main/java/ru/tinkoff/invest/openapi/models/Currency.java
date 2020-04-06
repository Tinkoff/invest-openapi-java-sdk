package ru.tinkoff.invest.openapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечисление возможных валютных позиций.
 */
public enum Currency {
    @JsonProperty("RUB")
    RUB,
    @JsonProperty("USD")
    USD,
    @JsonProperty("EUR")
    EUR,
    @JsonProperty("CHF")
    CHF,
    @JsonProperty("CNY")
    CNY,
    @JsonProperty("GBP")
    GPB,
    @JsonProperty("HKD")
    HKD,
    @JsonProperty("JPY")
    JPY,
    @JsonProperty("TRY")
    TRY
}
