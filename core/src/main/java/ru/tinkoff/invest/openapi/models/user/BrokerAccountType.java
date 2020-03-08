package ru.tinkoff.invest.openapi.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечислние возможных типов юрокерских счетов.
 */
public enum BrokerAccountType {
    @JsonProperty("Tinkoff")
    Tinkoff,
    @JsonProperty("TinkoffIis")
    TinkoffIis
}
