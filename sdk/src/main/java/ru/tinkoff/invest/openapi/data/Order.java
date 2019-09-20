package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Order {
    final private String id;
    final private String figi;
    final private OperationType operation;
    final private OrderStatus status;
    final private int requestedLots;
    final private int executedLots;
    final private OrderType type;
    final private BigDecimal price;

    @JsonCreator
    public Order(@JsonProperty("orderId")
                 String id,
                 @JsonProperty("figi")
                 String figi,
                 @JsonProperty("operation")
                 OperationType operation,
                 @JsonProperty("status")
                 OrderStatus status,
                 @JsonProperty("requestedLots")
                 int requestedLots,
                 @JsonProperty("executedLots")
                 int executedLots,
                 @JsonProperty("type")
                 OrderType type,
                 @JsonProperty("price")
                 BigDecimal price) {

        this.id = id;
        this.figi = figi;
        this.operation = operation;
        this.status = status;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.type = type;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getFigi() {
        return figi;
    }

    public OperationType getOperation() {
        return operation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getRequestedLots() {
        return requestedLots;
    }

    public int getExecutedLots() {
        return executedLots;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
