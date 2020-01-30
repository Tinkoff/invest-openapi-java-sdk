package ru.tinkoff.invest.openapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Модель ответа от REST API.
 *
 * @param <Payload> Тип полезной нагрузки ответа.
 */
public class RestResponse<Payload> {
    /**
     * Квази-уникальный идентификатор.
     */
    public final String trackingId;

    /**
     * Общий статус.
     */
    public final ResponseStatus status;

    /**
     * Полезная нагрузка.
     */
    public final Payload payload;

    /**
     * Создаёт ответ со всеми его компонентами.
     *
     * @param trackingId Идентификатор.
     * @param status Статус.
     * @param payload Нагрузка.
     */
    public RestResponse(@JsonProperty("trackingId")
                        String trackingId,
                        @JsonProperty("status")
                        ResponseStatus status,
                        @JsonProperty("payload")
                        Payload payload) {
        if (Objects.isNull(trackingId)) {
            throw new IllegalArgumentException("Идентификатор не может быть null.");
        }
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Статус не может быть null.");
        }
        if (Objects.isNull(payload)) {
            throw new IllegalArgumentException("Нагрузка не может быть null.");
        }

        this.trackingId = trackingId;
        this.status = status;
        this.payload = payload;
    }
}
