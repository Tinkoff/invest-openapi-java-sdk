package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Модель "свечи".
 */
public final class Candle {

    /**
     * Идентификатор инструмента.
     */
    @NotNull
    public final String figi;

    /**
     * Инетрвал времени описываемый "свечой".
     */
    @NotNull
    public final CandleInterval interval;

    /**
     * Цена открытия.
     */
    @JsonProperty(value = "o")
    @NotNull
    public final BigDecimal openPrice;

    /**
     * Цена закрытия.
     */
    @JsonProperty(value = "c")
    @NotNull
    public final BigDecimal closePrice;

    /**
     * Максимальная цена.
     */
    @JsonProperty(value = "h")
    @NotNull
    public final BigDecimal highestPrice;

    /**
     * Минимальная цена.
     */
    @JsonProperty(value = "l")
    @NotNull
    public final BigDecimal lowestPrice;

    /**
     * Объём торгов.
     */
    @JsonProperty(value = "v")
    @NotNull
    public final BigDecimal tradesValue;

    /**
     * Время формирования данной свечи.
     */
    @NotNull
    public final OffsetDateTime time;

    @JsonCreator
    public Candle(@JsonProperty(value = "figi", required = true)
                  @NotNull
                  final String figi,
                  @JsonProperty(value = "interval", required = true)
                  @NotNull
                  final CandleInterval interval,
                  @JsonProperty(value = "o", required = true)
                  @NotNull
                  final BigDecimal openPrice,
                  @JsonProperty(value = "c", required = true)
                  @NotNull
                  final BigDecimal closePrice,
                  @JsonProperty(value = "h", required = true)
                  @NotNull
                  final BigDecimal highestPrice,
                  @JsonProperty(value = "l", required = true)
                  @NotNull
                  final BigDecimal lowestPrice,
                  @JsonProperty(value = "v", required = true)
                  @NotNull
                  final BigDecimal tradesValue,
                  @JsonProperty(value = "time", required = true)
                  @NotNull
                  final OffsetDateTime time) {
        this.figi = figi;
        this.interval = interval;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.tradesValue = tradesValue;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Candle{" +
                "figi='" + figi + '\'' +
                ", interval=" + interval +
                ", openPrice=" + openPrice +
                ", closePrice=" + closePrice +
                ", highestPrice=" + highestPrice +
                ", lowestPrice=" + lowestPrice +
                ", tradesValue=" + tradesValue +
                ", time=" + time +
                '}';
    }
}
