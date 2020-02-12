package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Модель списка инструментов.
 */
public final class InstrumentsList {

    /**
     * Полное количество инструментов.
     */
    public final int total;

    /**
     * Непосредственно список инструментов.
     */
    @NotNull
    public final List<Instrument> instruments;

    @JsonCreator
    public InstrumentsList(@JsonProperty(value = "total", required = true)
                           final int total,
                           @JsonProperty(value = "instruments", required = true)
                           @NotNull
                           final List<Instrument> instruments) {
        this.total = total;
        this.instruments = instruments;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InstrumentsList(");
        sb.append("total=").append(total);
        sb.append(", instruments=").append(instruments);
        sb.append(')');
        return sb.toString();
    }
}
