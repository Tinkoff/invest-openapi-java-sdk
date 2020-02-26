package ru.tinkoff.invest.openapi.models.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.MoneyAmount;

import java.util.Objects;

/**
 * Модель размещённой биржевой заявки.
 */
public final class PlacedOrder {

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
    public PlacedOrder(@JsonProperty(value = "orderId", required = true)
                       @NotNull
                       final String id,
                       @JsonProperty(value = "operation", required = true)
                       @NotNull
                       final Operation operation,
                       @JsonProperty(value = "status", required = true)
                       @NotNull
                       final Status status,
                       @JsonProperty(value = "rejectReason")
                       @Nullable
                       final String rejectReason,
                       @JsonProperty(value = "message")
                       @Nullable
                       final String message,
                       @JsonProperty(value = "requestedLots", required = true)
                       final int requestedLots,
                       @JsonProperty(value = "executedLots", required = true)
                       final int executedLots,
                       @JsonProperty("commission")
                       @Nullable
                       final MoneyAmount commission) {
        this.id = id;
        this.operation = operation;
        this.status = status;
        this.rejectReason = rejectReason;
        this.message = message;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.commission = commission;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlacedLimitOrder(");
        sb.append("id='").append(id).append('\'');
        sb.append(", operation=").append(operation);
        sb.append(", status=").append(status);
        if (Objects.nonNull(rejectReason)) sb.append(", rejectReason='").append(rejectReason).append('\'');
        if (Objects.nonNull(message)) sb.append(", message='").append(message).append('\'');
        sb.append(", requestedLots=").append(requestedLots);
        sb.append(", executedLots=").append(executedLots);
        if (Objects.nonNull(commission)) sb.append(", commission=").append(commission);
        sb.append(')');
        return sb.toString();
    }

}

