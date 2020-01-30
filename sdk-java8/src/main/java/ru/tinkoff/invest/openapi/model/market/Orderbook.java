package ru.tinkoff.invest.openapi.model.market;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Модель биржевого "стакана".
 */
public class Orderbook {

    /**
     * Глубина стакана.
     */
    public final Integer depth;

    /**
     * Список выставленных заявок на продажу.
     */
    public final List<OrderbookItem> bids;

    /**
     * Список выставленных заявок на покупку.
     */
    public final List<OrderbookItem> asks;

    /**
     * Идентификатор инструмента.
     */
    public final String figi;

    /**
     * Текущий торговый статус инструмента.
     */
    public final TradeStatus tradeStatus;

    /**
     * Минимальный шаг цены по инструменту.
     */
    public final BigDecimal minPriceIncrement;

    /**
     * Цена последней совершённой сделки.
     * Может быть null.
     */
    public final BigDecimal lastPrice;

    /**
     * Цена закрытия.
     * Может быть null.
     */
    public final BigDecimal closePrice;

    /**
     * Верхний предел цены.
     * Может быть null.
     */
    public final BigDecimal limitUp;

    /**
     * Нижний предел цены.
     * Может быть null.
     */
    public final BigDecimal limitDown;

    @JsonCreator
    public Orderbook(@JsonProperty("depth")
                     Integer depth,
                     @JsonProperty("bids")
                     List<OrderbookItem> bids,
                     @JsonProperty("asks")
                     List<OrderbookItem> asks,
                     @JsonProperty("figi")
                     String figi,
                     @JsonProperty("tradeStatus")
                     TradeStatus tradeStatus,
                     @JsonProperty("minPriceIncrement")
                     BigDecimal minPriceIncrement,
                     @JsonProperty("lastPrice")
                     BigDecimal lastPrice,
                     @JsonProperty("closePrice")
                     BigDecimal closePrice,
                     @JsonProperty("limitUp")
                     BigDecimal limitUp,
                     @JsonProperty("limitDown")
                     BigDecimal limitDown) {
        if (Objects.isNull(depth)) {
            throw new IllegalArgumentException("Глубина не может быть null.");
        }
        if (Objects.isNull(bids)) {
            throw new IllegalArgumentException("Список заявок на покупку не может быть null.");
        }
        if (Objects.isNull(asks)) {
            throw new IllegalArgumentException("Список заявок на продажу не может быть null.");
        }
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
        }
        if (Objects.isNull(tradeStatus)) {
            throw new IllegalArgumentException("Торговый статус не может быть null.");
        }
        if (Objects.isNull(minPriceIncrement)) {
            throw new IllegalArgumentException("Минимальный шаг цены не может быть null.");
        }

        this.depth = depth;
        this.bids = bids;
        this.asks = asks;
        this.figi = figi;
        this.tradeStatus = tradeStatus;
        this.minPriceIncrement = minPriceIncrement;
        this.lastPrice = lastPrice;
        this.closePrice = closePrice;
        this.limitUp = limitUp;
        this.limitDown = limitDown;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Orderbook)) {
            return false;
        }

        final Orderbook other = (Orderbook)o;

        if (this.depth.intValue() != other.depth.intValue()) {
            return false;
        }
        if (!this.bids.equals(other.bids)) {
            return false;
        }
        if (!this.asks.equals(other.asks)) {
            return false;
        }
        if (!this.figi.equals(other.figi)) {
            return false;
        }

        return true;
    }

    /**
     * Модель элемента стакана.
     */
    public static class OrderbookItem {

        /**
         * Цена предложения.
         */
        public final BigDecimal price;

        /**
         * Количество предложений по цене.
         */
        public final BigDecimal quantity;

        @JsonCreator
        public OrderbookItem(@JsonProperty("price")
                             BigDecimal price,
                             @JsonProperty("quantity")
                             BigDecimal quantity) {
            if (Objects.isNull(price)) {
                throw new IllegalArgumentException("Цена не может быть null.");
            }
            if (Objects.isNull(quantity)) {
                throw new IllegalArgumentException("Количество не может быть null.");
            }

            this.price = price;
            this.quantity = quantity;
        }

        public boolean equals(Object o) {
            if (!(o instanceof OrderbookItem)) {
                return false;
            }

            final OrderbookItem other = (OrderbookItem)o;

            return this.price.equals(other.price) && this.quantity.equals(other.quantity);
        }
    }

    /**
     * Возможные торговые статусы.
     */
    public enum TradeStatus {
        @JsonProperty("NormalTrading")
        NormalTrading,
        @JsonProperty("NotAvailableForTrading")
        NotAvailableForTrading
    }
}
