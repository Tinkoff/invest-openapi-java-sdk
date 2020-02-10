package ru.tinkoff.invest.openapi.models.operations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Возможные статусы операции.
 */
public enum OperationStatus {
    @JsonProperty("Done")
    Done,
    @JsonProperty("Decline")
    Decline,
    @JsonProperty("Progress")
    Progress
}