package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlacedLimitOrder {
    final private String id;
    final private OperationType operation;
    final private String status;
    final private String rejectReason;
    final private int requestedLots;
    final private int executedLots;
    final private MoneyAmount commission;
    final private String figi;

    @JsonCreator
    public PlacedLimitOrder(@JsonProperty("orderId")
                            String id,
                            @JsonProperty("operation")
                            OperationType operation,
                            @JsonProperty("status")
                            String status,
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

    public String getStatus() {
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
