package ru.tinkoff.invest.openapi.model.streaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Интервал свечи.
 */
public enum CandleInterval {
    _1MIN("1min"),
    _2MIN("2min"),
    _3MIN("3min"),
    _5MIN("5min"),
    _10MIN("10min"),
    _15MIN("15min"),
    _30MIN("30min"),
    HOUR("hour"),
    _2HOUR("2hour"),
    _4HOUR("4hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String value;

    CandleInterval(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CandleInterval fromValue(String text) {
        for (CandleInterval b : CandleInterval.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
