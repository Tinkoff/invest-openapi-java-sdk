package ru.tinkoff.invest.openapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Модель ответа с ошибкой.
 */
public class ErrorPayload {

    /**
     * Текст ошибки.
     */
    public final String message;

    /**
     * Код ошибки.
     */
    public final String code;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param message Текст.
     * @param code Код.
     */
    @JsonCreator
    public ErrorPayload(@JsonProperty("message")
                        String message,
                        @JsonProperty("code")
                        String code) {
        if (Objects.isNull(message)) {
            throw new IllegalArgumentException("Текст ошибки не может быть null.");
        }
        if (Objects.isNull(code)) {
            throw new IllegalArgumentException("Код ошибки не может быть null.");
        }

        this.message = message;
        this.code = code;
    }
}
