package ru.tinkoff.invest.openapi.models.orders;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечисление возможных типов операций у заявок.
 */
public enum Operation {

    /**
     * Покупка.
     */
    @JsonProperty("Buy")
    Buy,

    /**
     * Продажа.
     */
    @JsonProperty("Sell")
    Sell

}
