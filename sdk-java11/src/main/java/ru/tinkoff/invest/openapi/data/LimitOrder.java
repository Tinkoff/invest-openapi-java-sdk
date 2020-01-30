package ru.tinkoff.invest.openapi.data;

import java.math.BigDecimal;

/**
 * Модель лимитной заявки.
 */
public class LimitOrder {

    /**
     * Идентификатор инструмента.
     */
    private final String figi;

    /**
     * Количество лотов.
     */
    private final int lots;

    /**
     * Тип операции.
     */
    private final OperationType operation;

    /**
     * Желаемая цена.
     */
    private final BigDecimal price;

    public LimitOrder(String figi, int lots, OperationType operation, BigDecimal price) {
        this.figi = figi;
        this.lots = lots;
        this.operation = operation;
        this.price = price;
    }

    public String getFigi() {
        return figi;
    }

    public int getLots() {
        return lots;
    }

    public OperationType getOperation() {
        return operation;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
