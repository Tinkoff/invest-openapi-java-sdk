package ru.tinkoff.invest.openapi.models.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Модель списка операций возвращаемая OpenAPI.
 */
public final class OperationsList {

    /**
     * Непосредственно список операций.
     */
    @NotNull
    public final List<Operation> operations;

    @JsonCreator
    public OperationsList(@JsonProperty(value = "operations", required = true)
                          @NotNull
                          final List<Operation> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OperationsList(");
        sb.append("operations=").append(operations);
        sb.append(')');
        return sb.toString();
    }
}
