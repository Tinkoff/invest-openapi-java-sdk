package ru.tinkoff.invest.openapi.models.streaming;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.util.Objects;

/**
 * Общий класс для моделей запросов на подписки в streaming.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="event")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StreamingRequest.CandleSubscribeRequest.class, name = "candle:subscribe"),
        @JsonSubTypes.Type(value = StreamingRequest.CandleUnsubscribeRequest.class, name = "candle:unsubscribe"),
        @JsonSubTypes.Type(value = StreamingRequest.OrderbookSubscribeRequest.class, name = "orderbook:subscribe"),
        @JsonSubTypes.Type(value = StreamingRequest.OrderbookUnsubscribeRequest.class, name = "orderbook:unsubscribe"),
        @JsonSubTypes.Type(value = StreamingRequest.InstrumentInfoSubscribeRequest.class, name = "instrument_info:subscribe"),
        @JsonSubTypes.Type(value = StreamingRequest.InstrumentInfoUnsubscribeRequest.class, name = "instrument_info:unsubscribe")
})
public abstract class StreamingRequest {

    /**
     * Идентификатор подписки.
     */
    protected final String requestId;

    protected StreamingRequest(@Nullable final String requestId) {
        this.requestId = requestId;
    }

    @Nullable
    public String getRequestId() {
        return requestId;
    }

    @NotNull
    public abstract String onOffPairId();

    public static abstract class ActivatingRequest extends StreamingRequest {
        protected ActivatingRequest(String requestId) {
            super(requestId);
        }
    }
    public static abstract class DeactivatingRequest extends StreamingRequest {
        protected DeactivatingRequest(String requestId) {
            super(requestId);
        }
    }

    /**
     * Запрос на подписку на поток событий о свечах.
     */
    public static class CandleSubscribeRequest extends ActivatingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Временной интервал свечей.
         */
        private final CandleInterval interval;

        CandleSubscribeRequest(@NotNull final String figi,
                               @NotNull final CandleInterval interval,
                               @Nullable final String requestId) {
            super(requestId);
            this.figi = figi;
            this.interval = interval;
        }

        CandleSubscribeRequest(@NotNull final String figi,
                               @NotNull final CandleInterval interval) {
            this(figi, interval, null);
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        @NotNull
        public CandleInterval getInterval() {
            return interval;
        }

        @NotNull
        @Override
        public String onOffPairId() {
            return new StringBuilder("Candle(")
                .append(figi)
                .append(",")
                .append(interval.name())
                .append(")")
                .toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CandleSubscribeRequest(");
            if (Objects.nonNull(requestId)) sb.append("requestId='").append(requestId).append('\'');
            sb.append(", figi='").append(figi).append('\'');
            sb.append(", interval=").append(interval);
            sb.append(')');
            return sb.toString();
        }
    }

    /**
     * Запрос на отписку от потока событий о свечах.
     */
    public static class CandleUnsubscribeRequest extends DeactivatingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Временной интервал свечей.
         */
        private final CandleInterval interval;

        CandleUnsubscribeRequest(@NotNull final String figi,
                                 @NotNull final CandleInterval interval,
                                 @Nullable final String requestId) {
            super(requestId);
            this.figi = figi;
            this.interval = interval;
        }

        CandleUnsubscribeRequest(@NotNull final String figi,
                                 @NotNull final CandleInterval interval) {
            this(figi, interval, null);
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        @NotNull
        public CandleInterval getInterval() {
            return interval;
        }

        @NotNull
        @Override
        public String onOffPairId() {
            return new StringBuilder("Candle(")
                    .append(figi)
                    .append(",")
                    .append(interval.name())
                    .append(")")
                    .toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CandleUnsubscribeRequest(");
            if (Objects.nonNull(requestId)) sb.append("requestId='").append(requestId).append('\'');
            sb.append(", figi='").append(figi).append('\'');
            sb.append(", interval=").append(interval);
            sb.append(')');
            return sb.toString();
        }
    }

    /**
     * Запрос на подписку на поток событий об изменениях в инструменте.
     */
    public static class InstrumentInfoSubscribeRequest extends ActivatingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        InstrumentInfoSubscribeRequest(@NotNull final String figi,
                                       @Nullable final String requestId) {
            super(requestId);
            this.figi = figi;
        }

        InstrumentInfoSubscribeRequest(@NotNull final String figi) {
            this(figi,null);
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        @NotNull
        @Override
        public String onOffPairId() {
            return new StringBuilder("InstrumentInfo(")
                    .append(figi)
                    .append(")")
                    .toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InstrumentInfoSubscribeRequest(");
            if (Objects.nonNull(requestId)) sb.append("requestId='").append(requestId).append('\'');
            sb.append(", figi='").append(figi).append('\'');
            sb.append(')');
            return sb.toString();
        }

    }

    /**
     * Запрос на отписку от потока событий об изменениях в инструменте.
     */
    public static class InstrumentInfoUnsubscribeRequest extends DeactivatingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        InstrumentInfoUnsubscribeRequest(@NotNull final String figi,
                                         @Nullable final String requestId) {
            super(requestId);
            this.figi = figi;
        }

        InstrumentInfoUnsubscribeRequest(@NotNull final String figi) {
            this(figi, null);
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        @NotNull
        @Override
        public String onOffPairId() {
            return new StringBuilder("InstrumentInfo(")
                    .append(figi)
                    .append(")")
                    .toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InstrumentInfoUnsubscribeRequest(");
            if (Objects.nonNull(requestId)) sb.append("requestId='").append(requestId).append('\'');
            sb.append(", figi='").append(figi).append('\'');
            sb.append(')');
            return sb.toString();
        }

    }

    /**
     * Запрос на подписку на поток событий об изменениях биржевого стакана.
     */
    public static class OrderbookSubscribeRequest extends ActivatingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Глубина стакана.
         */
        private final int depth;

        OrderbookSubscribeRequest(@NotNull final String figi,
                                  final int depth,
                                  @Nullable final String requestId) {
            super(requestId);
            if (depth < 0  || depth > 20) throw new IllegalArgumentException("Глубина должна быть от 1 до 20");
            this.figi = figi;
            this.depth = depth;
        }

        OrderbookSubscribeRequest(@NotNull final String figi,
                                  final int depth) {
            this(figi, depth, null);
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        public int getDepth() {
            return depth;
        }

        @NotNull
        @Override
        public String onOffPairId() {
            return new StringBuilder("Orderbook(")
                    .append(figi)
                    .append(",")
                    .append(depth)
                    .append(")")
                    .toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("OrderbookSubscribeRequest(");
            if (Objects.nonNull(requestId)) sb.append("requestId='").append(requestId).append('\'');
            sb.append(", figi='").append(figi).append('\'');
            sb.append(", depth=").append(depth);
            sb.append(')');
            return sb.toString();
        }

    }

    /**
     * Запрос на отписку от потока событий об изменениях биржевого стакана.
     */
    public static class OrderbookUnsubscribeRequest extends DeactivatingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Глубина стакана.
         */
        private final int depth;

        OrderbookUnsubscribeRequest(@NotNull final String figi,
                                    final int depth,
                                    @Nullable final String requestId) {
            super(requestId);
            if (depth < 0  || depth > 20) throw new IllegalArgumentException("Глубина должна быть от 1 до 20");
            this.figi = figi;
            this.depth = depth;
        }

        OrderbookUnsubscribeRequest(@NotNull final String figi,
                                    final int depth) {
            this(figi, depth, null);
        }

        @NotNull
        public String getFigi() {
            return figi;
        }

        public int getDepth() {
            return depth;
        }

        @NotNull
        @Override
        public String onOffPairId() {
            return new StringBuilder("Orderbook(")
                    .append(figi)
                    .append(",")
                    .append(depth)
                    .append(")")
                    .toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("OrderbookUnsubscribeRequest(");
            if (Objects.nonNull(requestId)) sb.append("requestId='").append(requestId).append('\'');
            sb.append(", figi='").append(figi).append('\'');
            sb.append(", depth=").append(depth);
            sb.append(')');
            return sb.toString();
        }

    }

    public static CandleSubscribeRequest subscribeCandle(@NotNull final String figi,
                                                         @NotNull final CandleInterval interval) {
        return new StreamingRequest.CandleSubscribeRequest(figi, interval);
    }

    public static CandleUnsubscribeRequest unsubscribeCandle(@NotNull final String figi,
                                                             @NotNull final CandleInterval interval) {
        return new StreamingRequest.CandleUnsubscribeRequest(figi, interval);
    }

    public static OrderbookSubscribeRequest subscribeOrderbook(@NotNull final String figi, final int depth) {
        return new StreamingRequest.OrderbookSubscribeRequest(figi, depth);
    }

    public static OrderbookUnsubscribeRequest unsubscribeOrderbook(@NotNull final String figi, final int depth) {
        return new StreamingRequest.OrderbookUnsubscribeRequest(figi, depth);
    }

    public static InstrumentInfoSubscribeRequest subscribeInstrumentInfo(@NotNull final String figi) {
        return new StreamingRequest.InstrumentInfoSubscribeRequest(figi);
    }

    public static InstrumentInfoUnsubscribeRequest unsubscribeInstrumentInfo(@NotNull final String figi) {
        return new StreamingRequest.InstrumentInfoUnsubscribeRequest(figi);
    }
}
