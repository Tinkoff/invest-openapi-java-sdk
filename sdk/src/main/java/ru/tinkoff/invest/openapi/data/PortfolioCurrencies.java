package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioCurrencies {

    private final List<PortfolioCurrency> currencies;

    public static class PortfolioCurrency {
        private final Currency currency;
        private final BigDecimal balance;
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
