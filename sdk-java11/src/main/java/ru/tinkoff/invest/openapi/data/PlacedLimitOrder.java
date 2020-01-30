package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель размещённой биржевой заявки.
 */
public class PlacedLimitOrder {

    /**
     * Идентификатор заявки.
     */
    final private String id;

    /**
     * Тип операции.
     */
    final private OperationType operation;

    /**
     * Текущий статус.
     */
    final private OrderStatus status;

    /**
     * Причина отказа в размещении.
     * Может быть null.
     */
    final private String rejectReason;

    /**
     * Желаемое количество лотов.
     */
    final private int requestedLots;

    /**
     * Реально исполненное количество лотов.
     */
    final private int executedLots;

    /**
     * Размер коммиссии.
     *
     */
    final private MoneyAmount commission;

    /**
     * Идентификатор инструмента.
     */
    final private String figi;

    @JsonCreator
    public PlacedLimitOrder(@JsonProperty("orderId")
                            String id,
                            @JsonProperty("operation")
                            OperationType operation,
                            @JsonProperty("status")
                            OrderStatus status,
                            @JsonProperty("rejectReason")
                            String rejectReason,
                            @JsonProperty("requestedLots")
                            int requestedLots,
                            @JsonProperty("executedLots")
                            int executedLots,
                            @JsonProperty("commission")
                            MoneyAmount commission,
                            @JsonProperty("figi")
                            String figi) {
        this.id = id;
        this.operation = operation;
        this.status = status;
        this.rejectReason = rejectReason;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.commission = commission;
        this.figi = figi;
    }

    public String getId() {
        return id;
    }

    public OperationType getOperation() {
        return operation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public int getRequestedLots() {
        return requestedLots;
    }

    public int getExecutedLots() {
        return executedLots;
    }

    public MoneyAmount getCommission() {
        return commission;
    }

    public String getFigi() {
        return figi;
    }
}
