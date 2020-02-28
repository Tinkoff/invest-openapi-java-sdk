package ru.tinkoff.invest.openapi.models.orders;

import org.jetbrains.annotations.NotNull;

/**
 * Модель лимитной заявки.
 */
public final class MarketOrder {

    /**
     * Количество лотов.
     */
    public final int lots;

    /**
     * Тип операции.
     */
    @NotNull
    public final Operation operation;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param lots Количество лотов.
     * @param operation Тип операции.
     */
    public MarketOrder(final int lots,
                       @NotNull final Operation operation) {
        if (lots < 1) {
            throw new IllegalArgumentException("Количество лотов должно быть положительным.");
        }

        this.lots = lots;
        this.operation = operation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MarketOrder(");
        sb.append("lots=").append(lots);
        sb.append(", operation=").append(operation);
        sb.append(')');
        return sb.toString();
    }
}
