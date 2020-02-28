package ru.tinkoff.invest.openapi.models.sandbox;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Данные для запроса на выставление баланса по позиции.
 */
public final class PositionBalance {

    /**
     * Идентификатор инструмента, по которому необходимо произвести выставление баланса.
     */
    @NotNull
    public final String figi;

    /**
     * Значение баланса, которое необходимо выставить.
     */
    @NotNull
    public final BigDecimal balance;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param figi Идентификатор инструмнта.
     * @param balance Значение баланса.
     */
    public PositionBalance(@NotNull final String figi,
                           @NotNull final BigDecimal balance) {
        this.figi = figi;
        this.balance = balance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PositionBalance(");
        sb.append("figi='").append(figi).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(')');
        return sb.toString();
    }

}
