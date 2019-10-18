package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Модель биржевой заявки.
 */
public class Order {

    /**
     * Идентификатор заявки.
     */
    final private String id;

    /**
     * Идентификатор инструмента.
     */
    final private String figi;

    /**
     * Тип операции.
     */
    final private OperationType operation;

    /**
     * Текущий статус.
     */
    final private OrderStatus status;

    /**
     * Запрашиваемое количество лотов.
     */
    final private int requestedLots;

    /**
     * Фактически исполненное количество лотов.
     */
    final private int executedLots;

    /**
     * Тип заявки.
     */
    final private OrderType type;

    /**
     * Желаемая цена.
     */
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
