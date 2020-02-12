package ru.tinkoff.invest.openapi.models;

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
                        final String message,
                        @JsonProperty(value = "code", required = true)
                        @NotNull
                        final String code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorPayload(");
        sb.append("message='").append(message).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(')');
        return sb.toString();
    }

}
