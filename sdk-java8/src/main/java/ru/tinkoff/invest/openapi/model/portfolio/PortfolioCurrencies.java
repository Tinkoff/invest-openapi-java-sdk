package ru.tinkoff.invest.openapi.model.portfolio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tinkoff.invest.openapi.model.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Модель портфеля валют.
 */
public class PortfolioCurrencies {

    /**
     * Список валютных позиций.
     */
    public final List<PortfolioCurrency> currencies;

    /**
     * Модель валютной позиции.
     */
    public static class PortfolioCurrency {

        /**
         * Валюта.
         */
        public final Currency currency;

        /**
         * Объём позиции.
         */
        public final BigDecimal balance;

        /**
         * Заблокированный объём.
         * Может быть null.
         */
        public final BigDecimal blocked;

        @JsonCreator
        public PortfolioCurrency(@JsonProperty("currency")
                                 Currency currency,
                                 @JsonProperty("balance")
                                 BigDecimal balance,
                                 @JsonProperty("blocked")
                                 BigDecimal blocked) {
            if (Objects.isNull(currency)) {
                throw new IllegalArgumentException("Валюта не может быть null.");
            }
            if (Objects.isNull(balance)) {
                throw new IllegalArgumentException("Значание баланса не может быть null.");
            }

            this.currency = currency;
            this.balance = balance;
            this.blocked = blocked;
        }

    }

    @JsonCreator
    public PortfolioCurrencies(@JsonProperty("currencies")
                               List<PortfolioCurrency> currencies) {
        if (Objects.isNull(currencies)) {
            throw new IllegalArgumentException("Список позиций не может быть null.");
        }

        this.currencies = currencies;
    }

}
