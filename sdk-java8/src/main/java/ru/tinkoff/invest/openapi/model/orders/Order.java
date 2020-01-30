package ru.tinkoff.invest.openapi.model.orders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель биржевой заявки.
 */
public final class Order {

    /**
     * Идентификатор заявки.
     */
    final public String id;

    /**
     * Идентификатор инструмента.
     */
    final public String figi;

    /**
     * Тип операции заявки.
     */
    final public Operation operation;

    /**
     * Текущий статус.
     */
    final public Status status;

    /**
     * Запрашиваемое для исполнения количество лотов.
     */
    final public Integer requestedLots;

    /**
     * Фактически исполненное количество лотов.
     */
    final public Integer executedLots;

    /**
     * Тип заявки.
     */
    final public Type type;

    /**
     * Желаемая цена.
     */
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
    public Order(@JsonProperty("orderId")
                 String id,
                 @JsonProperty("figi")
                 String figi,
                 @JsonProperty("operation")
                 Operation operation,
                 @JsonProperty("status")
                 Status status,
                 @JsonProperty("requestedLots")
                 Integer requestedLots,
                 @JsonProperty("executedLots")
                 Integer executedLots,
                 @JsonProperty("type")
                 Type type,
                 @JsonProperty("price")
                 BigDecimal price) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("Идентификатор заявки не может быть null.");
        }
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
        }
        if (Objects.isNull(operation)) {
            throw new IllegalArgumentException("Тип операции заявки не может быть null.");
        }
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Статус заявки не может быть null.");
        }
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("Тип заявки не может быть null.");
        }
        if (Objects.isNull(price)) {
            throw new IllegalArgumentException("Цена заявки не может быть null.");
        }
        if (Objects.isNull(requestedLots)) {
            throw new IllegalArgumentException("Запрашиваемое количество лотов не может быть null.");
        }
        if (Objects.isNull(executedLots)) {
            throw new IllegalArgumentException("Исполненное количество лотов не может быть null.");
        }

        this.id = id;
        this.figi = figi;
        this.operation = operation;
        this.status = status;
        this.requestedLots = requestedLots;
        this.executedLots = executedLots;
        this.type = type;
        this.price = price;
    }
}
