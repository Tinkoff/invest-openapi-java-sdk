package ru.tinkoff.invest.openapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MoneyAmount(");
        sb.append("currency=").append(currency);
        sb.append(", value=").append(value);
        sb.append(')');
        return sb.toString();
    }

}
