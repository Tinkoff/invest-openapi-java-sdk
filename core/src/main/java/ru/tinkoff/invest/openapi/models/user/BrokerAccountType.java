package ru.tinkoff.invest.openapi.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечислние возможных типов операции.
 */
public enum BrokerAccountType {
    @JsonProperty("Tinkoff")
    Tinkoff,
    @JsonProperty("TinkoffIis ")
    TinkoffIis
}
