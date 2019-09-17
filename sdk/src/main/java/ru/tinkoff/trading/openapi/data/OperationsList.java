package ru.tinkoff.trading.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OperationsList {
    private final List<Operation> operations;

    @JsonCreator
    public OperationsList(@JsonProperty("total")
                          int total,
                          @JsonProperty("operations")
                          List<Operation> operations) {
        this.operations = operations;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
