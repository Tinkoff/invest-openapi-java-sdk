package ru.tinkoff.invest.openapi.models.streaming;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Общий класс для моделей событий приходящих из streaming.
 */
@JsonDeserialize(using = StreamingEvent.StreamingEventDeserializer.class)
public abstract class StreamingEvent {

    /**
     * Модель события с изменением свечи.
     */
    @JsonDeserialize
    public static class Candle extends StreamingEvent {

        /**
         * Цена открытия.
         */
        private final BigDecimal openPrice;

        /**
         * Цена закрытия.
         */
        private final BigDecimal closingPrice;

        /**
         * Цена макисмальная цена.
         */
        private final BigDecimal highestPrice;

        /**
         * Минимальная цена.
         */
        private final BigDecimal lowestPrice;

        /**
         * Объём торгов.
         */
        private final BigDecimal tradingValue;

        /**
         * Дата/время формирования свечи.
         */
        private final ZonedDateTime dateTime;

        /**
         * Временной интервал свечи.
         */
        private final CandleInterval interval;

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        @JsonCreator
        public Candle(@JsonProperty(value = "o", required = true)
                      @NotNull
                      final BigDecimal openPrice,
                      @JsonProperty(value = "c", required = true)
                      @NotNull
                      final BigDecimal closingPrice,
                      @JsonProperty(value = "h", required = true)
                      @NotNull
                      final BigDecimal highestPrice,
                      @JsonProperty(value = "l", required = true)
                      @NotNull
                      final BigDecimal lowestPrice,
                      @JsonProperty(value = "v", required = true)
                      @NotNull
                      final BigDecimal tradingValue,
                      @JsonProperty(value = "time", required = true)
                      @NotNull
                      final ZonedDateTime dateTime,
                      @JsonProperty(value = "interval", required = true)
                      @NotNull
                      final CandleInterval interval,
                      @JsonProperty(value = "figi", required = true)
                      @NotNull
                      final String figi) {
            this.openPrice = openPrice;
            this.closingPrice = closingPrice;
            this.highestPrice = highestPrice;
            this.lowestPrice = lowestPrice;
            this.tradingValue = tradingValue;
            this.dateTime = dateTime;
            this.interval = interval;
            this.figi = figi;
        }

        @NotNull
        public BigDecimal getOpenPrice() {
            return openPrice;
        }

        @NotNull
        public BigDecimal getClosingPrice() {
            return closingPrice;
        }

        @NotNull
        public BigDecimal getHighestPrice() {
            return highestPrice;
        }

        @NotNull
        public BigDecimal getLowestPrice() {
            return lowestPrice;
        }

        @NotNull
        public BigDecimal getTradingValue() {
            return tradingValue;
        }

        @NotNull
        public ZonedDateTime getDateTime() {
            return dateTime;
        }

        @NotNull
        public CandleInterval getInterval() {
            return interval;
        }

        @NotNull
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

            final Candle other = (Candle) o;

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

    /**
     * Модель события с изменением стакана.
     */
    @JsonDeserialize
    public static class Orderbook extends StreamingEvent {

        /**
         * Глубина стакана.
         */
        private final int depth;

        /**
         * Список размещённых предложений о продаже.
         */
        private final List<BigDecimal[]> bids;

        /**
         * Список размещённых предложений о покупке.
         */
        private final List<BigDecimal[]> asks;

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        @JsonCreator
        public Orderbook(@JsonProperty(value = "depth", required = true)
                         final int depth,
                         @JsonProperty(value = "bids", required = true)
                         @NotNull
                         final List<BigDecimal[]> bids,
                         @NotNull
                         @JsonProperty(value = "asks", required = true)
                         final List<BigDecimal[]> asks,
                         @JsonProperty(value = "figi", required = true)
                         @NotNull
                         final String figi) {
            this.depth = depth;
            this.bids = bids;
            this.asks = asks;
            this.figi = figi;
        }

        public int getDepth() {
            return depth;
        }

        @NotNull
        public List<BigDecimal[]> getBids() {
            return bids;
        }

        @NotNull
        public List<BigDecimal[]> getAsks() {
            return asks;
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        @Override
        public String toString() {
            final List<String> bidsString = bids.stream()
                    .map(x -> Arrays.toString(Arrays.stream(x).map(BigDecimal::toPlainString).toArray(String[]::new)))
                    .collect(Collectors.toList());
            final List<String> asksString = asks.stream()
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

            final Orderbook other = (Orderbook) o;

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

    /**
     * Модель события с изменением состояния инструмента.
     */
    @JsonDeserialize
    public static class InstrumentInfo extends StreamingEvent {

        /**
         * Текущий торговый статус инструмента.
         */
        private final String tradeStatus;

        /**
         * Минимальный шаг цены.
         */
        private final BigDecimal minPriceIncrement;

        /**
         * Размер лота.
         */
        private final int lot;

        /**
         * Накопленный купонный доход (НКД).
         * Только у бондов не null.
         */
        private final BigDecimal accruedInterest;

        /**
         * Верхняя граница заявки.
         * Не null только для RTS инструментов.
         */
        private final BigDecimal limitUp;

        /**
         * Нижняя граница заявки.
         * Не null только для RTS инструментов.
         */
        private final BigDecimal limitDown;

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        @JsonCreator
        public InstrumentInfo(@JsonProperty(value = "trade_status", required = true)
                              @NotNull
                              final String tradeStatus,
                              @JsonProperty(value = "min_price_increment", required = true)
                              @NotNull
                              final BigDecimal minPriceIncrement,
                              @JsonProperty(value = "lot", required = true)
                              final int lot,
                              @JsonProperty("accrued_interest")
                              @Nullable
                              final BigDecimal accruedInterest,
                              @JsonProperty("limit_up")
                              @Nullable
                              final BigDecimal limitUp,
                              @JsonProperty("limit_down")
                              @Nullable
                              final BigDecimal limitDown,
                              @JsonProperty(value = "figi", required = true)
                              @NotNull
                              final String figi) {
            this.tradeStatus = tradeStatus;
            this.minPriceIncrement = minPriceIncrement;
            this.lot = lot;
            this.accruedInterest = accruedInterest;
            this.limitUp = limitUp;
            this.limitDown = limitDown;
            this.figi = figi;
        }

        @NotNull
        public String getTradeStatus() {
            return tradeStatus;
        }

        @NotNull
        public BigDecimal getMinPriceIncrement() {
            return minPriceIncrement;
        }

        public int getLot() {
            return lot;
        }

        @Nullable
        public BigDecimal getAccruedInterest() {
            return accruedInterest;
        }

        @Nullable
        public BigDecimal getLimitUp() {
            return limitUp;
        }

        @Nullable
        public BigDecimal getLimitDown() {
            return limitDown;
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        public boolean canTrade() {
            return tradeStatus.equals("normal_trading");
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InstrumentInfo(");
            sb.append("tradeStatus='").append(tradeStatus).append('\'');
            sb.append(", minPriceIncrement=").append(minPriceIncrement);
            sb.append(", lot=").append(lot);
            if (Objects.nonNull(accruedInterest)) sb.append(", accruedInterest=").append(accruedInterest);
            if (Objects.nonNull(limitUp)) sb.append(", limitUp=").append(limitUp);
            if (Objects.nonNull(limitDown)) sb.append(", limitDown=").append(limitDown);
            sb.append(", figi='").append(figi).append('\'');
            sb.append(')');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InstrumentInfo)) {
                return false;
            }

            final InstrumentInfo other = (InstrumentInfo) o;

            if (!this.tradeStatus.equals(other.tradeStatus)) {
                return false;
            }
            if (!this.minPriceIncrement.equals(other.minPriceIncrement)) {
                return false;
            }
            if (this.lot != other.lot) {
                return false;
            }
            if (!Objects.equals(this.accruedInterest, other.accruedInterest)) {
                return false;
            }
            if (!Objects.equals(this.limitUp, other.limitUp)) {
                return false;
            }
            if (!Objects.equals(this.limitDown, other.limitDown)) {
                return false;
            }
            if (!this.figi.equals(other.figi)) {
                return false;
            }

            return true;
        }
    }

    /**
     * Модель сообщения об ошибке пришедшей из streaming.
     */
    @JsonDeserialize
    public static class Error extends StreamingEvent {

        /**
         * Текст ошибки.
         */
        private final String error;

        /**
         * Идентификатор подписки.
         */
        private final String requestId;

        @JsonCreator
        public Error(@JsonProperty(value = "error", required = true)
                     @NotNull
                     final String error,
                     @JsonProperty("request_id")
                     @Nullable
                     final String requestId) {
            this.error = error;
            this.requestId = requestId;
        }

        @NotNull
        public String getError() {
            return error;
        }

        @Nullable
        public String getRequestId() {
            return requestId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Error(");
            sb.append("error='").append(error).append('\'');
            if (Objects.nonNull(requestId)) sb.append(", requestId='").append(requestId).append('\'');
            sb.append(')');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Error)) {
                return false;
            }

            final Error other = (Error) o;

            if (!this.error.equals(other.error)) {
                return false;
            }
            if (!Objects.equals(this.requestId, other.requestId)) {
                return false;
            }

            return true;
        }
    }

    static class StreamingEventDeserializer extends StdDeserializer<StreamingEvent> {

        private static final long serialVersionUID = -4598785717730517692L;

        public StreamingEventDeserializer() {
            this(null);
        }

        protected StreamingEventDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public StreamingEvent deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            final ObjectNode node = (ObjectNode) p.getCodec().readTree(p);
            final JsonNode eventNameNode = node.get("event");

            if (eventNameNode == null || !eventNameNode.isTextual()) {
                throw new JsonParseException(p, "No type field 'event'.");
            }

            final String eventName = eventNameNode.asText();
            final JsonNode payloadNode = node.get("payload");

            if (payloadNode == null || !payloadNode.isObject()) {
                throw new JsonParseException(p, "No data field 'payload'.");
            }

            final ObjectMapper mapper = new ObjectMapper();
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
                case "error":
                    result = mapper.readValue(mapper.writeValueAsString(payloadNode), Error.class);
                    break;
                default:
                    throw new JsonParseException(p, "Unknown event type.");
            }

            p.close();

            return result;
        }
    }
}
