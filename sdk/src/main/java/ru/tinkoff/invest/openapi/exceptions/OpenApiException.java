package ru.tinkoff.invest.openapi.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenApiException extends Exception {
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
