package ru.tinkoff.invest.openapi.models.orders;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Модель лимитной заявки.
 */
public final class LimitOrder {

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
     * Желаемая цена.
     */
    @NotNull
    public final BigDecimal price;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * Выбрасывает {@code IllegalArgumentException} при указании лотов менее, чем 1.
     *
     * @param lots Количество лотов.
     * @param operation Тип операции.
     * @param price Желаемая цена.
     */
    public LimitOrder(final int lots,
                      @NotNull final Operation operation,
                      @NotNull final BigDecimal price) {
        if (lots < 1) {
            throw new IllegalArgumentException("Количество лотов должно быть положительным.");
        }

        this.lots = lots;
        this.operation = operation;
        this.price = price;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LimitOrder(");
        sb.append("lots=").append(lots);
        sb.append(", operation=").append(operation);
        sb.append(", price=").append(price);
        sb.append(')');
        return sb.toString();
    }
}
