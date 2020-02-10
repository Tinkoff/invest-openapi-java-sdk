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
    EUR
}
