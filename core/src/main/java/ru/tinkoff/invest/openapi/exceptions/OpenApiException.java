package ru.tinkoff.invest.openapi.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 * Исключение возникающее при ошибках в выполнении запросов к OpenAPI.
 */
public class OpenApiException extends Exception {

    private static final long serialVersionUID = -1549345261742621592L;
    
    /**
     * Код ошибки в OpenAPI.
     */
    private final String code;

    @JsonCreator
    public OpenApiException(@JsonProperty("message")
                            @NotNull
                            final String message,
                            @JsonProperty("code")
                            @NotNull
                            final String code) {
        super(message);
        this.code = code;
    }

    @NotNull
    public String getCode() {
        return code;
    }
    
}
