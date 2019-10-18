package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class Orderbook {
    private final int depth;
    private final List<OrderbookItem> bids;
    private final List<OrderbookItem> asks;
    private final String figi;
    private final TradeStatus tradeStatus;
    private final BigDecimal minPriceIncrement;
    private final BigDecimal lastPrice;
    private final BigDecimal closePrice;
    private final BigDecimal limitUp;
    private final BigDecimal limitDown;

    @JsonCreator
    public Orderbook(@JsonProperty("depth")
                     int depth,
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

    public int getDepth() {
        return depth;
    }

    public List<OrderbookItem> getBids() {
        return bids;
    }

    public List<OrderbookItem> getAsks() {
        return asks;
    }

    public String getFigi() {
        return figi;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public BigDecimal getMinPriceIncrement() {
        return minPriceIncrement;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getLimitUp() {
        return limitUp;
    }

    public BigDecimal getLimitDown() {
        return limitDown;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Orderbook)) {
            return false;
        }

        final var other = (Orderbook)o;

        if (this.depth != other.depth) {
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

    public static class OrderbookItem {
        private final BigDecimal price;
        private final BigDecimal quantity;

        @JsonCreator
        public OrderbookItem(@JsonProperty("price")
                             BigDecimal price,
                             @JsonProperty("quantity")
                             BigDecimal quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public boolean equals(Object o) {
            if (!(o instanceof OrderbookItem)) {
                return false;
            }

            final var other = (OrderbookItem)o;

            return this.price.equals(other.price) && this.quantity.equals(other.quantity);
        }
    }

    public enum TradeStatus {
        @JsonProperty("NormalTrading")
        NormalTrading,
        @JsonProperty("NotAvailableForTrading")
        NotAvailableForTrading
    }
}
