package ru.tinkoff.invest.openapi.model.orders;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель лимитной заявки.
 */
public final class LimitOrder {

    /**
     * Идентификатор инструмента.
     */
    public final String figi;

    /**
     * Количество лотов.
     */
    public final Integer lots;

    /**
     * Тип операции.
     */
    public final Operation operation;

    /**
     * Желаемая цена.
     */
    public final BigDecimal price;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param figi Идентификатор инструмента.
     * @param lots Количество лотов.
     * @param operation Тип операции.
     * @param price Желаемая цена.
     */
    public LimitOrder(String figi, Integer lots, Operation operation, BigDecimal price) {
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
        }
        if (Objects.isNull(lots)) {
            throw new IllegalArgumentException("Количество не может быть null.");
        }
        if (Objects.isNull(operation)) {
            throw new IllegalArgumentException("Тип операции заявки не может быть null.");
        }
        if (Objects.isNull(price)) {
            throw new IllegalArgumentException("Цена инструмента не может быть null.");
        }

        this.figi = figi;
        this.lots = lots;
        this.operation = operation;
        this.price = price;
    }
}
