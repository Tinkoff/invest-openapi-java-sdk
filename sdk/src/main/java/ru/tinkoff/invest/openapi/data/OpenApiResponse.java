package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель ответа REST-методов OpenAPI.
 *
 * @param <P> Тип полезной нагрузки ответа.
 */
public class OpenApiResponse<P> {

    /**
     * Идентификатор запроса.
     */
    final public String trackingId;

    /**
     * Статус исполнения.
     */
    final public String status;

    /**
     * Полезная нагрузка.
     */
    final public P payload;

    @JsonCreator
    public OpenApiResponse(@JsonProperty("trackingId")
                           String trackingId,
                           @JsonProperty("status")
                           String status,
                           @JsonProperty("payload")
                           P payload) {
        this.trackingId = trackingId;
        this.status = status;
        this.payload = payload;
    }
}
