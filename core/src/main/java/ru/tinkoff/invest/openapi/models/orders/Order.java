package ru.tinkoff.invest.openapi.models.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Модель биржевой заявки.
 */
public final class Order {

    /**
     * Идентификатор заявки.
     */
    @NotNull
    final public String id;

    /**
     * Идентификатор инструмента.
     */
    @NotNull
    final public String figi;

    /**
     * Тип операции заявки.
     */
    @NotNull
    final public Operation operation;

    /**
     * Текущий статус.
     */
    @NotNull
    final public Status status;

    /**
     * Запрашиваемое для исполнения количество лотов.
     */
    final public int requestedLots;

    /**
     * Фактически исполненное количество лотов.
     */
    final public int executedLots;

    /**
     * Тип заявки.
     */
    @NotNull
    final public OrderType type;

    /**
     * Желаемая цена.
     */
    @NotNull
    final public BigDecimal price;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param id Идентификатор заявки.
     * @param figi Идентификатор инструмента.
     * @param operation Тип операции.
     * @param status Текущий статус.
     * @param requestedLots Запрашиваемое количество лотов.
     * @param executedLots Исполненное количество лотов.
     * @param type Тип заявки.
     * @param price Цена.
     */
    @JsonCreator
    public Order(@JsonProperty(value = "orderId", required = true)
                 @NotNull
                 final String id,
                 @JsonProperty(value = "figi", required = true)
                 @NotNull
                 final String figi,
                 @JsonProperty(value = "operation", required = true)
                 @NotNull
                 final Operation operation,
                 @JsonProperty(value = "status", required = true)
                 @NotNull
                 final Status status,
                 @JsonProperty(value = "requestedLots", required = true)
                 int requestedLots,
                 @JsonProperty(value = "executedLots", required = true)
                 int executedLots,
                 @JsonProperty(value = "type", required = true)
                 @NotNull
                 final OrderType type,
                 @JsonProperty(value = "price", required = true)
                 @NotNull
                 final BigDecimal price) {
        this.id = id;
        this.figi = figi;
        this.operation = operation;
        this.status = status;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.type = type;
        this.price = price;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Order(");
        sb.append("id='").append(id).append('\'');
        sb.append(", figi='").append(figi).append('\'');
        sb.append(", operation=").append(operation);
        sb.append(", status=").append(status);
        sb.append(", requestedLots=").append(requestedLots);
        sb.append(", executedLots=").append(executedLots);
        sb.append(", type=").append(type);
        sb.append(", price=").append(price);
        sb.append(')');
        return sb.toString();
    }

}
