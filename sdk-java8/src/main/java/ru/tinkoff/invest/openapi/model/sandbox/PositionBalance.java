package ru.tinkoff.invest.openapi.model.sandbox;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Данные для запроса на выставление баланса по позиции.
 */
public final class PositionBalance {

    /**
     * Идентификатор инструмента, по которому необходимо произвести выставление баланса.
     */
    public final String figi;

    /**
     * Значение баланса, которое необходимо выставить.
     */
    public final BigDecimal balance;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param figi Идентификатор инструмнта.
     * @param balance Значение баланса.
     */
    public PositionBalance(String figi, BigDecimal balance) {
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
        }
        if (Objects.isNull(balance)) {
            throw new IllegalArgumentException("Значение валанса не может быть null.");
        }

        this.figi = figi;
        this.balance = balance;
    }
}
