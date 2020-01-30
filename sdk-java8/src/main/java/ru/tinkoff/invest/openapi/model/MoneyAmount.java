package ru.tinkoff.invest.openapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель денежных средств.
 */
public class MoneyAmount {

    /**
     * Валюта средств.
     */
    public final Currency currency;

    /**
     * Размер средств.
     */
    public final BigDecimal value;

    @JsonCreator
    public MoneyAmount(@JsonProperty("currency")
                       Currency currency,
                       @JsonProperty("value")
                       BigDecimal value) {
        if (Objects.isNull(currency)) {
            throw new IllegalArgumentException("Валюта не может быть null.");
        }
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("Размер не может быть null.");
        }

        this.currency = currency;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MoneyAmount)) {
            return false;
        }
        final MoneyAmount other = (MoneyAmount)o;

        return this.currency == other.currency && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "MoneyAmount{" +
                "currency=" + currency +
                ", value=" + value.toPlainString() +
                '}';
    }
}
