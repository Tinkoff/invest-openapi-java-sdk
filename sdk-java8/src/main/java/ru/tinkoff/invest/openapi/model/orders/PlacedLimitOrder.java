package ru.tinkoff.invest.openapi.model.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.MoneyAmount;

/**
 * Модель размещённой биржевой заявки.
 */
public class PlacedLimitOrder {

    /**
     * Идентификатор заявки.
     */
    @NotNull
    public final String id;

    /**
     * Тип операции.
     */
    @NotNull
    public final Operation operation;

    /**
     * Текущий статус.
     */
    @NotNull
    public final Status status;

    /**
     * Код причина отказа в размещении.
     */
    @Nullable
    public final String rejectReason;

    /**
     * Причина отказа в размещении (человеческий текст).
     */
    @Nullable
    public final String message;

    /**
     * Желаемое количество лотов.
     */
    public final int requestedLots;

    /**
     * Реально исполненное количество лотов.
     */
    public final int executedLots;

    /**
     * Размер коммиссии.
     */
    @Nullable
    public final MoneyAmount commission;

    @JsonCreator
    public PlacedLimitOrder(@JsonProperty(value = "orderId", required = true)
                            @NotNull
                                    String id,
                            @JsonProperty(value = "operation", required = true)
                            @NotNull
                                    Operation operation,
                            @JsonProperty(value = "status", required = true)
                            @NotNull
                                    Status status,
                            @JsonProperty(value = "rejectReason")
                            @Nullable
                                    String rejectReason,
                            @JsonProperty(value = "message")
                            @Nullable
                                    String message,
                            @JsonProperty(value = "requestedLots", required = true)
                                    int requestedLots,
                            @JsonProperty(value = "executedLots", required = true)
                                    int executedLots,
                            @JsonProperty("commission")
                            @Nullable
                                    MoneyAmount commission) {
        this.id = id;
        this.operation = operation;
        this.status = status;
        this.rejectReason = rejectReason;
        this.message = message;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.commission = commission;
    }

}

