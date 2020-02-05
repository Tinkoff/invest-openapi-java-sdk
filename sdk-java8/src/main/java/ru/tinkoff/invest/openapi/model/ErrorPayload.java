package ru.tinkoff.invest.openapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 * Модель ответа с ошибкой.
 */
public class ErrorPayload {

    /**
     * Текст ошибки.
     */
    @NotNull
    public final String message;

    /**
     * Код ошибки.
     */
    @NotNull
    public final String code;

    /**
     * Создаёт экземпляр со всеми его компонентами.
     *
     * @param message Текст.
     * @param code Код.
     */
    @JsonCreator
    public ErrorPayload(@JsonProperty(value = "message", required = true)
                        @NotNull
                                String message,
                        @JsonProperty(value = "code", required = true)
                        @NotNull
                                String code) {
        this.message = message;
        this.code = code;
    }
}
