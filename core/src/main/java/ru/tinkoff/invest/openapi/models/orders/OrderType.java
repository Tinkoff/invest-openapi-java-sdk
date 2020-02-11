package ru.tinkoff.invest.openapi.models.orders;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечисление возможных типов заявок.
 */
public enum OrderType {

    /**
     * Лимитная заявка.
     */
    @JsonProperty("Limit")
    Limit,

    /**
     * Рыночная заявка.
     */
    @JsonProperty("Market")
    Market

}
