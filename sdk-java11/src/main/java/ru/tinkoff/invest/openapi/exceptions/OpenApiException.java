package ru.tinkoff.invest.openapi.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Исключение возникающее при ошибках в выполнении запросов к OpenAPI.
 */
public class OpenApiException extends Exception {

    /**
     * Код ошибки в OpenAPI.
     */
    private final String code;

    @JsonCreator
    public OpenApiException(@JsonProperty("message")
                            String message,
                            @JsonProperty("code")
                            String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
