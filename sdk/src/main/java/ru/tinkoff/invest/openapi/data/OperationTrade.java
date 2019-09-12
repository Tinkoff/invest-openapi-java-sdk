package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class OperationTrade {
    private final String tradeId;
    private final ZonedDateTime date;
    private final BigDecimal price;
    private final int quantity;

    @JsonCreator
    public OperationTrade(@JsonProperty("tradeId")
                          String tradeId,
                          @JsonProperty("date")
                          ZonedDateTime date,
                          @JsonProperty("price")
                          BigDecimal price,
                          @JsonProperty("quantity")
                          int quantity) {
        this.tradeId = tradeId;
        this.date = date;
        this.price = price;
        this.quantity = quantity;
    }

    public String getTradeId() {
        return tradeId;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OperationTrade)) {
            return false;
        }
        final var other = (OperationTrade)o;

        return this.tradeId.equals(other.tradeId) &&
                this.date.equals(other.date) &&
                this.price.equals(other.price) &&
                this.quantity == other.quantity;
    }
}
