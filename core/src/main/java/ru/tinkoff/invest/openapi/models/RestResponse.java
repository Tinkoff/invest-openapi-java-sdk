package ru.tinkoff.invest.openapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.jetbrains.annotations.NotNull;

/**
 * Модель ответа от REST API.
 *
 * @param <Payload> Тип полезной нагрузки ответа.
 */
public class RestResponse<Payload> {

    /**
     * Квази-уникальный идентификатор.
     */
    @NotNull
    public final String trackingId;

    /**
     * Общий статус.
     */
    @NotNull
    public final ResponseStatus status;

    /**
     * Полезная нагрузка.
     */
    @NotNull
    public final Payload payload;

    /**
     * Создаёт ответ со всеми его компонентами.
     *
     * @param trackingId Идентификатор.
     * @param status Статус.
     * @param payload Нагрузка.
     */
    public RestResponse(@JsonProperty("trackingId")
                        @NotNull
                        final String trackingId,
                        @JsonProperty("status")
                        @NotNull
                        final ResponseStatus status,
                        @JsonProperty("payload")
                        @NotNull
                        final Payload payload) {
        this.trackingId = trackingId;
        this.status = status;
        this.payload = payload;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestResponse(");
        sb.append("trackingId='").append(trackingId).append('\'');
        sb.append(", status=").append(status);
        sb.append(", payload=").append(payload);
        sb.append(')');
        return sb.toString();
    }
}
