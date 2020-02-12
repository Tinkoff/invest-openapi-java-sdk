package ru.tinkoff.invest.openapi.models.portfolio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.Currency;

import java.math.BigDecimal;
import java.util.List;

/**
 * Модель портфеля валют.
 */
public final class PortfolioCurrencies {

    /**
     * Список валютных позиций.
     */
    @NotNull
    public final List<PortfolioCurrency> currencies;

    @JsonCreator
    public PortfolioCurrencies(@JsonProperty("currencies")
                               @NotNull
                               final List<PortfolioCurrency> currencies) {
        this.currencies = currencies;
    }

    /**
     * Модель валютной позиции.
     */
    public final static class PortfolioCurrency {

        /**
         * Валюта.
         */
        @NotNull
        public final Currency currency;

        /**
         * Объём позиции.
         */
        @NotNull
        public final BigDecimal balance;

        /**
         * Заблокированный объём.
         */
        @Nullable
        public final BigDecimal blocked;

        @JsonCreator
        public PortfolioCurrency(@JsonProperty("currency")
                                 @NotNull
                                 final Currency currency,
                                 @JsonProperty("balance")
                                 @NotNull
                                 final BigDecimal balance,
                                 @JsonProperty("blocked")
                                 @Nullable
                                 final BigDecimal blocked) {
            this.currency = currency;
            this.balance = balance;
            this.blocked = blocked;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PortfolioCurrency(");
            sb.append("currency=").append(currency);
            sb.append(", balance=").append(balance);
            sb.append(", blocked=").append(blocked);
            sb.append(')');
            return sb.toString();
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PortfolioCurrencies(");
        sb.append("currencies=").append(currencies);
        sb.append(')');
        return sb.toString();
    }
}
