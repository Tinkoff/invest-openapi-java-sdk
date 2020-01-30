package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Модель списка операций возвращаемая OpenAPI.
 */
public class OperationsList {

    /**
     * Непосредственно список операций.
     */
    private final List<Operation> operations;

    @JsonCreator
    public OperationsList(@JsonProperty("operations")
                          List<Operation> operations) {
        this.operations = operations;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
