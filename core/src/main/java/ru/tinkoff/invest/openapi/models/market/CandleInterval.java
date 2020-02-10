package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Возможные интервалы времени у свечей.
 */
public enum CandleInterval {
    @JsonProperty("1min")
    ONE_MIN,
    @JsonProperty("2min")
    TWO_MIN,
    @JsonProperty("3min")
    THREE_MIN,
    @JsonProperty("5min")
    FIVE_MIN,
    @JsonProperty("10min")
    TEN_MIN,
    @JsonProperty("15min")
    QUARTER_HOUR,
    @JsonProperty("30min")
    HALF_HOUR,
    @JsonProperty("hour")
    HOUR,
    @JsonProperty("2hour")
    TWO_HOURS,
    @JsonProperty("4hour")
    FOUR_HOURS,
    @JsonProperty("day")
    DAY,
    @JsonProperty("week")
    WEEK,
    @JsonProperty("month")
    MONTH
}
