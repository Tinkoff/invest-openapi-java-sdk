package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(using = StreamingEvent.StreamingEventDeserializer.class)
public abstract class StreamingEvent {
    @JsonDeserialize
    public static class Candle extends StreamingEvent {
        private final BigDecimal openPrice;
        private final BigDecimal closingPrice;
        private final BigDecimal highestPrice;
        private final BigDecimal lowestPrice;
        private final BigDecimal tradingValue;
        private final ZonedDateTime dateTime;
        private final CandleInterval interval;
        private final String figi;

        @JsonCreator
        public Candle(@JsonProperty("o")
                      BigDecimal openPrice,
                      @JsonProperty("c")
                      BigDecimal closingPrice,
                      @JsonProperty("h")
                      BigDecimal highestPrice,
                      @JsonProperty("l")
                      BigDecimal lowestPrice,
                      @JsonProperty("v")
                      BigDecimal tradingValue,
                      @JsonProperty("time")
                      ZonedDateTime dateTime,
                      @JsonProperty("interval")
                              CandleInterval interval,
                      @JsonProperty("figi")
                      String figi) {
            this.openPrice = openPrice;
            this.closingPrice = closingPrice;
            this.highestPrice = highestPrice;
            this.lowestPrice = lowestPrice;
            this.tradingValue = tradingValue;
            this.dateTime = dateTime;
            this.interval = interval;
            this.figi = figi;
        }

        public BigDecimal getOpenPrice() {
            return openPrice;
        }

        public BigDecimal getClosingPrice() {
            return closingPrice;
        }

        public BigDecimal getHighestPrice() {
            return highestPrice;
        }

        public BigDecimal getLowestPrice() {
            return lowestPrice;
        }

        public BigDecimal getTradingValue() {
            return tradingValue;
        }

        public ZonedDateTime getDateTime() {
            return dateTime;
        }

        public CandleInterval getInterval() {
            return interval;
        }

        public String getFigi() {
            return figi;
        }

        @Override
        public String toString() {
            return "Candle(openPrice = " + openPrice +
                    ", closingPrice = " + closingPrice +
                    ", highestPrice = " + highestPrice +
                    ", lowestPrice = " + lowestPrice +
                    ", tradingValue = " + tradingValue +
                    ", dateTime = " + dateTime +
                    ", interval = " + interval +
                    ", figi = " + figi +
                    ")";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Candle)) {
                return false;
            }

            final var other = (Candle)o;

            if (!this.closingPrice.equals(other.closingPrice)) {
                return false;
            }
            if (!this.dateTime.equals(other.dateTime)) {
                return false;
            }
            if (!this.figi.equals(other.figi)) {
                return false;
            }
            if (!this.highestPrice.equals(other.highestPrice)) {
                return false;
            }
            if (this.interval != other.interval) {
                return false;
            }
            if (!this.openPrice.equals(other.openPrice)) {
                return false;
            }
            if (!this.tradingValue.equals(other.tradingValue)) {
                return false;
            }

            return true;
        }
    }

    @JsonDeserialize
    public static class Orderbook extends StreamingEvent {
        private final int depth;
        private final List<BigDecimal[]> bids;
        private final List<BigDecimal[]> asks;
        private final String figi;

        @JsonCreator
        public Orderbook(@JsonProperty("depth")
                         int depth,
                         @JsonProperty("bids")
                         List<BigDecimal[]> bids,
                         @JsonProperty("asks")
                         List<BigDecimal[]> asks,
                         @JsonProperty("figi")
                         String figi) {
            this.depth = depth;
            this.bids = bids;
            this.asks = asks;
            this.figi = figi;
        }

        public int getDepth() {
            return depth;
        }

        public List<BigDecimal[]> getBids() {
            return bids;
        }

        public List<BigDecimal[]> getAsks() {
            return asks;
        }

        public String getFigi() {
            return figi;
        }

        @Override
        public String toString() {
            final var bidsString = bids.stream()
                    .map(x -> Arrays.toString(Arrays.stream(x).map(BigDecimal::toPlainString).toArray(String[]::new)))
                    .collect(Collectors.toList());
            final var asksString = asks.stream()
                    .map(x -> Arrays.toString(Arrays.stream(x).map(BigDecimal::toPlainString).toArray(String[]::new)))
                    .collect(Collectors.toList());
            return "Orderbook(depth = " + depth +
                    ", bids = " + bidsString +
                    ", asks = " + asksString +
                    ", figi = " + figi +
                    ")";
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
    }

    @JsonDeserialize
    public static class InstrumentInfo extends StreamingEvent {
        private final String tradeStatus;
        private final BigDecimal minPriceIncrement;
        private final int lot;
        private final BigDecimal accruedInterest;
        private final BigDecimal limitUp;
        private final BigDecimal limitDown;
        private final String figi;

        @JsonCreator
        public InstrumentInfo(@JsonProperty("trade_status")
                              String tradeStatus,
                              @JsonProperty("min_price_increment")
                              BigDecimal minPriceIncrement,
                              @JsonProperty("lot")
                              int lot,
                              @JsonProperty("accrued_interest")
                              BigDecimal accruedInterest,
                              @JsonProperty("limit_up")
                              BigDecimal limitUp,
                              @JsonProperty("limit_down")
                              BigDecimal limitDown,
                              @JsonProperty("figi")
                              String figi) {
            this.tradeStatus = tradeStatus;
            this.minPriceIncrement = minPriceIncrement;
            this.lot = lot;
            this.accruedInterest = accruedInterest;
            this.limitUp = limitUp;
            this.limitDown = limitDown;
            this.figi = figi;
        }

        public String getTradeStatus() {
            return tradeStatus;
        }

        public BigDecimal getMinPriceIncrement() {
            return minPriceIncrement;
        }

        public int getLot() {
            return lot;
        }

        public BigDecimal getAccruedInterest() {
            return accruedInterest;
        }

        public BigDecimal getLimitUp() {
            return limitUp;
        }

        public BigDecimal getLimitDown() {
            return limitDown;
        }

        public String getFigi() {
            return figi;
        }

        public boolean canTrade() {
            return tradeStatus.equals("normal_trading");
        }

        @Override
        public String toString() {
            return "InstrumentInfo(tradeStatus = " + tradeStatus +
                    ", minPriceIncrement = " + minPriceIncrement +
                    ", lot = " + lot +
                    ", accruedInterest = " + accruedInterest +
                    ", limitUp = " + limitUp +
                    ", limitDown = " + limitDown +
                    ", figi = " + figi +
                    ")";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InstrumentInfo)) {
                return false;
            }

            final var other = (InstrumentInfo)o;

            if (!this.tradeStatus.equals(other.tradeStatus)) {
                return false;
            }
            if (!this.minPriceIncrement.equals(other.minPriceIncrement)) {
                return false;
            }
            if (this.lot != other.lot) {
                return false;
            }
            if (!this.accruedInterest.equals(other.accruedInterest)) {
                return false;
            }
            if (!this.limitUp.equals(other.limitUp)) {
                return false;
            }
            if (!this.limitDown.equals(other.limitDown)) {
                return false;
            }
            if (!this.figi.equals(other.figi)) {
                return false;
            }

            return true;
        }
    }

    static class StreamingEventDeserializer extends StdDeserializer<StreamingEvent> {

        public StreamingEventDeserializer() {
            this(null);
        }

        protected StreamingEventDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public StreamingEvent deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            final var node = (ObjectNode) p.getCodec().readTree(p);
            final var eventNameNode = node.get("event");

            if (eventNameNode == null || !eventNameNode.isTextual()) {
                throw new JsonParseException(p, "No type field 'event'.");
            }

            final var eventName = eventNameNode.asText();
            final var payloadNode = node.get("payload");

            if (payloadNode == null || !payloadNode.isObject()) {
                throw new JsonParseException(p, "No data field 'payload'.");
            }

            final var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            StreamingEvent result;
            switch (eventName) {
                case "candle":
                    result = mapper.readValue(mapper.writeValueAsString(payloadNode), Candle.class);
                    break;
                case "orderbook":
                    result = mapper.readValue(mapper.writeValueAsString(payloadNode), Orderbook.class);
                    break;
                case "instrument_info":
                    result = mapper.readValue(mapper.writeValueAsString(payloadNode), InstrumentInfo.class);
                    break;
                default:
                    throw new JsonParseException(p, "Unknown event type.");
            }

            p.close();

            return result;
        }
    }
}
