package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Модель валютного портфеля возвращаемая OpenAPI.
 */
public class PortfolioCurrencies {

    /**
     * Список валютных позиций.
     */
    private final List<PortfolioCurrency> currencies;

    /**
     * Модель валютной позиции.
     */
    public static class PortfolioCurrency {

        /**
         * Валюта.
         */
        private final Currency currency;

        /**
         * Объём позиции.
         */
        private final BigDecimal balance;

        /**
         * Заблокированный объём.
         */
        private final BigDecimal blocked;

        @JsonCreator
        public PortfolioCurrency(@JsonProperty("currency")
                                 Currency currency,
                                 @JsonProperty("balance")
                                 BigDecimal balance,
                                 @JsonProperty("blocked")
                                 BigDecimal blocked) {
            this.currency = currency;
            this.balance = balance;
            this.blocked = blocked;
        }

        public Currency getCurrency() {
            return currency;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getBlocked() {
            return blocked;
        }
    }

    @JsonCreator
    public PortfolioCurrencies(@JsonProperty("currencies")
                               List<PortfolioCurrency> currencies) {
        this.currencies = currencies;
    }

    public List<PortfolioCurrency> getCurrencies() {
        return currencies;
    }

}
