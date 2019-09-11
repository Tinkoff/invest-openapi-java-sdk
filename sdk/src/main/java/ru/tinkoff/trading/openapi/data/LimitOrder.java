package ru.tinkoff.trading.openapi.data;

import java.math.BigDecimal;

public class LimitOrder {
    private final String figi;
    private final int lots;
    private final OperationType operation;
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
