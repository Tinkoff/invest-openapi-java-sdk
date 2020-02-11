package ru.tinkoff.invest.openapi.models.orders;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечисление возможных статусов заявки.
 */
public enum Status {
    @JsonProperty("New")
    New,
    @JsonProperty("PartiallyFill")
    PartiallyFill,
    @JsonProperty("Fill")
    Fill,
    @JsonProperty("Cancelled")
    Cancelled,
    @JsonProperty("Replaced")
    Replaced,
    @JsonProperty("PendingCancel")
    PendingCancel,
    @JsonProperty("Rejected")
    Rejected,
    @JsonProperty("PendingReplace")
    PendingReplace,
    @JsonProperty("PendingNew")
    PendingNew
}
