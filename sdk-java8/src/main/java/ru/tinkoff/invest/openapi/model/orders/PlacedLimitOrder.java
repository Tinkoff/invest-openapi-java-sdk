package ru.tinkoff.invest.openapi.model.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tinkoff.invest.openapi.model.MoneyAmount;

import java.util.Objects;

/**
 * Модель размещённой биржевой заявки.
 */
public class PlacedLimitOrder {

    /**
     * Идентификатор заявки.
     */
    public final String id;

    /**
     * Тип операции.
     */
    public final Operation operation;

    /**
     * Текущий статус.
     */
    public final Status status;

    /**
     * Причина отказа в размещении.
     * Может быть null.
     */
    public final String rejectReason;

    /**
     * Желаемое количество лотов.
     */
    public final Integer requestedLots;

    /**
     * Реально исполненное количество лотов.
     */
    public final Integer executedLots;

    /**
     * Размер коммиссии.
     * Может быть null.
     */
    public final MoneyAmount commission;

    @JsonCreator
    public PlacedLimitOrder(@JsonProperty("orderId")
                                    String id,
                            @JsonProperty("operation")
                                    Operation operation,
                            @JsonProperty("status")
                                    Status status,
                            @JsonProperty("rejectReason")
                                    String rejectReason,
                            @JsonProperty("requestedLots")
                                    Integer requestedLots,
                            @JsonProperty("executedLots")
                                    Integer executedLots,
                            @JsonProperty("commission")
                                    MoneyAmount commission) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("Идентификатор не может быть null.");
        }
        if (Objects.isNull(operation)) {
            throw new IllegalArgumentException("Тип операции не может быть null.");
        }
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Статус не может быть null.");
        }
        if (Objects.isNull(requestedLots)) {
            throw new IllegalArgumentException("Желаемое количество лотов не может быть null.");
        }
        if (Objects.isNull(executedLots)) {
            throw new IllegalArgumentException("Исполненное количество лотов не может быть null.");
        }

        this.id = id;
        this.operation = operation;
        this.status = status;
        this.rejectReason = rejectReason;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.commission = commission;
    }

}

