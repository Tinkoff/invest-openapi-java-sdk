package ru.tinkoff.invest.openapi.model.sandbox;

import ru.tinkoff.invest.openapi.model.Currency;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Данные для запроса на выставление баланса по валюте.
 */
public final class CurrencyBalance {

    /**
     * Валюта, по которой необходимо произвести выставление баланса.
     */
    public final Currency currency;

    /**
     * Значение баланса, которое необходимо выставить.
     */
    public final BigDecimal balance;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param currency Валюта.
     * @param balance Значение баланса.
     */
    public CurrencyBalance(Currency currency, BigDecimal balance) {
        if (Objects.isNull(currency)) {
            throw new IllegalArgumentException("Валюта не может быть null.");
        }
        if (Objects.isNull(balance)) {
            throw new IllegalArgumentException("Значение валанса не может быть null.");
        }

        this.currency = currency;
        this.balance = balance;
    }
}
