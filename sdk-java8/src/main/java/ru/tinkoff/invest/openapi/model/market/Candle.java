package ru.tinkoff.invest.openapi.model.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Модель "свечи".
 */
public class Candle {

    /**
     * Идентификатор инструмента.
     */
    public final String figi;

    /**
     * Инетрвал времени описываемый "свечой".
     */
    public final CandleInterval interval;

    /**
     * Цена открытия.
     */
    public final BigDecimal openPrice;

    /**
     * Цена закрытия.
     */
    public final BigDecimal closePrice;

    /**
     * Максимальная цена.
     */
    public final BigDecimal highestPrice;

    /**
     * Минимальная цена.
     */
    public final BigDecimal lowestPrice;

    /**
     * Объём торгов.
     */
    public final BigDecimal tradesValue;

    /**
     * Время формирования данной свечи.
     */
    public final OffsetDateTime time;

    @JsonCreator
    public Candle(@JsonProperty("figi")
                  String figi,
                  @JsonProperty("interval")
                  CandleInterval interval,
                  @JsonProperty("o")
                  BigDecimal openPrice,
                  @JsonProperty("c")
                  BigDecimal closePrice,
                  @JsonProperty("h")
                  BigDecimal highestPrice,
                  @JsonProperty("l")
                  BigDecimal lowestPrice,
                  @JsonProperty("v")
                  BigDecimal tradesValue,
                  @JsonProperty("time")
                  OffsetDateTime time) {
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
        }
        if (Objects.isNull(interval)) {
            throw new IllegalArgumentException("Интервал не может быть null.");
        }
        if (Objects.isNull(openPrice)) {
            throw new IllegalArgumentException("Цена открытия не может быть null.");
        }
        if (Objects.isNull(closePrice)) {
            throw new IllegalArgumentException("Цена закрытия не может быть null.");
        }
        if (Objects.isNull(highestPrice)) {
            throw new IllegalArgumentException("Максимальная цена не может быть null.");
        }
        if (Objects.isNull(lowestPrice)) {
            throw new IllegalArgumentException("Минимальная цена не может быть null.");
        }
        if (Objects.isNull(tradesValue)) {
            throw new IllegalArgumentException("Объём торгов не может быть null.");
        }
        if (Objects.isNull(time)) {
            throw new IllegalArgumentException("Время свечи не может быть null.");
        }

        this.figi = figi;
        this.interval = interval;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.tradesValue = tradesValue;
        this.time = time;
    }

}
