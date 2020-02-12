package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Модель биржевого "стакана".
 */
public final class Orderbook {

    /**
     * Идентификатор инструмента.
     */
    @NotNull
    public final String figi;

    /**
     * Глубина стакана.
     */
    public final int depth;

    /**
     * Список выставленных заявок на продажу.
     */
    @NotNull
    public final List<Item> bids;

    /**
     * Список выставленных заявок на покупку.
     */
    @NotNull
    public final List<Item> asks;

    /**
     * Текущий торговый статус инструмента.
     */
    @NotNull
    public final TradeStatus tradeStatus;

    /**
     * Минимальный шаг цены по инструменту.
     */
    @NotNull
    public final BigDecimal minPriceIncrement;

    /**
     * Номинал для облигации.
     */
    @Nullable
    public final BigDecimal faceValue;

    /**
     * Цена последней совершённой сделки.
     */
    @Nullable
    public final BigDecimal lastPrice;

    /**
     * Цена закрытия.
     */
    @Nullable
    public final BigDecimal closePrice;

    /**
     * Верхний предел цены.
     */
    @Nullable
    public final BigDecimal limitUp;

    /**
     * Нижний предел цены.
     */
    @Nullable
    public final BigDecimal limitDown;

    @JsonCreator
    public Orderbook(@JsonProperty(value = "figi", required = true)
                     @NotNull
                     final String figi,
                     @JsonProperty(value = "depth", required = true)
                     final int depth,
                     @JsonProperty(value = "bids", required = true)
                     @NotNull
                     final List<Item> bids,
                     @JsonProperty(value = "asks", required = true)
                     @NotNull
                     final List<Item> asks,
                     @JsonProperty(value = "tradeStatus", required = true)
                     @NotNull
                     final TradeStatus tradeStatus,
                     @JsonProperty(value = "minPriceIncrement", required = true)
                     @NotNull
                     final BigDecimal minPriceIncrement,
                     @JsonProperty("faceValue")
                     @Nullable
                     final BigDecimal faceValue,
                     @JsonProperty("lastPrice")
                     @Nullable
                     final BigDecimal lastPrice,
                     @JsonProperty("closePrice")
                     @Nullable
                     final BigDecimal closePrice,
                     @JsonProperty("limitUp")
                     @Nullable
                     final BigDecimal limitUp,
                     @JsonProperty("limitDown")
                     @Nullable
                     final BigDecimal limitDown) {
        this.depth = depth;
        this.bids = bids;
        this.asks = asks;
        this.figi = figi;
        this.tradeStatus = tradeStatus;
        this.minPriceIncrement = minPriceIncrement;
        this.faceValue = faceValue;
        this.lastPrice = lastPrice;
        this.closePrice = closePrice;
        this.limitUp = limitUp;
        this.limitDown = limitDown;
    }

    /**
     * Модель элемента стакана.
     */
    public final static class Item {

        /**
         * Цена предложения.
         */
        @NotNull
        public final BigDecimal price;

        /**
         * Количество предложений по цене.
         */
        @NotNull
        public final BigDecimal quantity;

        @JsonCreator
        public Item(@JsonProperty(value = "price", required = true)
                    @NotNull
                    final BigDecimal price,
                    @JsonProperty(value = "quantity", required = true)
                    @NotNull
                    final BigDecimal quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Item(");
            sb.append("price=").append(price);
            sb.append(", quantity=").append(quantity);
            sb.append(')');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Orderbook(");
        sb.append("figi='").append(figi).append('\'');
        sb.append(", depth=").append(depth);
        sb.append(", bids=").append(bids);
        sb.append(", asks=").append(asks);
        sb.append(", tradeStatus=").append(tradeStatus);
        sb.append(", minPriceIncrement=").append(minPriceIncrement);
        if (Objects.nonNull(faceValue)) sb.append(", faceValue=").append(faceValue);
        if (Objects.nonNull(lastPrice)) sb.append(", lastPrice=").append(lastPrice);
        if (Objects.nonNull(closePrice)) sb.append(", closePrice=").append(closePrice);
        if (Objects.nonNull(limitUp)) sb.append(", limitUp=").append(limitUp);
        if (Objects.nonNull(limitDown)) sb.append(", limitDown=").append(limitDown);
        sb.append(')');
        return sb.toString();
    }
}
