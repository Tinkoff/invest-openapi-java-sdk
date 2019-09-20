package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenApiResponse<P> {
    final public String trackingId;
    final public String status;
    final public P payload;

    @JsonCreator
    public OpenApiResponse(@JsonProperty("trackingId")
                           String trackingId,
                           @JsonProperty("status")
                           String status,
                           @JsonProperty("payload")
                           P payload) {
        this.trackingId = trackingId;
        this.status = status;
        this.payload = payload;
    }
}
