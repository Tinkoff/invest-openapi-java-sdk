package ru.tinkoff.invest.openapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Преречисление возможных статусов ответа от REST API.
 */
public enum ResponseStatus {
    /**
     * Успешный ответ.
     */
    @JsonProperty("Ok")
    OK,

    /**
     * Ответ с ошибкой.
     */
    @JsonProperty("Error")
    ERROR
}
