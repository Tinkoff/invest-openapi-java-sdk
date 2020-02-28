package ru.tinkoff.invest.openapi.models.sandbox;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.Currency;

import java.math.BigDecimal;

/**
 * Данные для запроса на выставление баланса по валюте.
 */
public final class CurrencyBalance {

    /**
     * Валюта, по которой необходимо произвести выставление баланса.
     */
    @NotNull
    public final Currency currency;

    /**
     * Значение баланса, которое необходимо выставить.
     */
    @NotNull
    public final BigDecimal balance;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param currency Валюта.
     * @param balance Значение баланса.
     */
    public CurrencyBalance(@NotNull final Currency currency,
                           @NotNull final BigDecimal balance) {
        this.currency = currency;
        this.balance = balance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CurrencyBalance(");
        sb.append("currency=").append(currency);
        sb.append(", balance=").append(balance);
        sb.append(')');
        return sb.toString();
    }

}
