package ru.tinkoff.trading.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class InstrumentsList {
    private final int total;
    private final List<Instrument> instruments;

    @JsonCreator
    public InstrumentsList(@JsonProperty("total")
                           int total,
                           @JsonProperty("instruments")
                           List<Instrument> instruments) {
        this.total = total;
        this.instruments = instruments;
    }

    public int getTotal() {
        return total;
    }

    public List<Instrument> getInstruments() {
        return instruments;
    }
}
