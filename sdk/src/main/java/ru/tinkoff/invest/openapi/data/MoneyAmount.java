package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class MoneyAmount {
    private final Currency currency;
    private final BigDecimal value;

    @JsonCreator
    public MoneyAmount(@JsonProperty("currency")
                       Currency currency,
                       @JsonProperty("value")
                       BigDecimal value) {
        this.currency = currency;
        this.value = value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MoneyAmount)) {
            return false;
        }
        final var other = (MoneyAmount)o;

        return this.currency == other.currency && this.value.equals(other.value);
    }
}
