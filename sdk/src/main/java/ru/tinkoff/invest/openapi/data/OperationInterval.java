package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OperationInterval {
    @JsonProperty("1day")
    DAY,
    @JsonProperty("7days")
    WEEK,
    @JsonProperty("14days")
    TWO_WEEKS,
    @JsonProperty("30days")
    MONTH;

    public String toParamString() {
        switch (this) {
            case WEEK:
                return "7days";
            case TWO_WEEKS:
                return "14days";
            case MONTH:
                return "30days";
            case DAY:
            default:
                return "1day";
        }
    }
}
