package ru.tinkoff.invest.openapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель денежных средств.
 */
public class MoneyAmount {

    /**
     * Валюта средств.
     */
    @NotNull
    public final Currency currency;

    /**
     * Размер средств.
     */
    @NotNull
    public final BigDecimal value;

    @JsonCreator
    public MoneyAmount(@JsonProperty("currency")
                       @NotNull
                       final Currency currency,
                       @JsonProperty("value")
                       @NotNull
                       final BigDecimal value) {
        this.currency = currency;
        this.value = value;
    }
}
