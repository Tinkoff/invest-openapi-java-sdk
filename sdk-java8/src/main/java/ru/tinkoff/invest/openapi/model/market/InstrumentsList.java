package ru.tinkoff.invest.openapi.model.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Модель списка инструментов.
 */
public class InstrumentsList {

    /**
     * Полное количество инструментов.
     */
    public final Integer total;

    /**
     * Непосредственно список инструментов.
     */
    public final List<Instrument> instruments;

    @JsonCreator
    public InstrumentsList(@JsonProperty("total")
                                   Integer total,
                           @JsonProperty("instruments")
                                   List<Instrument> instruments) {
        if (Objects.isNull(total)) {
            throw new IllegalArgumentException("Полное количество не может быть null.");
        }
        if (Objects.isNull(instruments)) {
            throw new IllegalArgumentException("Список инструментов не может быть null.");
        }

        this.total = total;
        this.instruments = instruments;
    }

}
