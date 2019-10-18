package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
     * Может быть null.
     */
    protected final String requestId;

    protected StreamingRequest(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    /**
     * Запрос на подписку на поток событий о свечах.
     */
    public static class CandleSubscribeRequest extends StreamingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Временной интервал свечей.
         */
        private final CandleInterval interval;

        CandleSubscribeRequest(String figi, CandleInterval interval, String requestId) {
            super(requestId);
            this.figi = figi;
            this.interval = interval;
        }

        CandleSubscribeRequest(String figi, CandleInterval interval) {
            this(figi, interval, null);
        }

        public String getFigi() {
            return figi;
        }

        public CandleInterval getInterval() {
            return interval;
        }
    }

    /**
     * Запрос на отписку от потока событий о свечах.
     */
    public static class CandleUnsubscribeRequest extends StreamingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Временной интервал свечей.
         */
        private final CandleInterval interval;

        CandleUnsubscribeRequest(String figi, CandleInterval interval, String requestId) {
            super(requestId);
            this.figi = figi;
            this.interval = interval;
        }

        CandleUnsubscribeRequest(String figi, CandleInterval interval) {
            this(figi, interval, null);
        }

        public String getFigi() {
            return figi;
        }

        public CandleInterval getInterval() {
            return interval;
        }
    }

    /**
     * Запрос на подписку на поток событий об изменениях в инструменте.
     */
    public static class InstrumentInfoSubscribeRequest extends StreamingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        InstrumentInfoSubscribeRequest(String figi, String requestId) {
            super(requestId);
            this.figi = figi;
        }

        InstrumentInfoSubscribeRequest(String figi) {
            this(figi,null);
        }

        public String getFigi() {
            return figi;
        }
    }

    /**
     * Запрос на отписку от потока событий об изменениях в инструменте.
     */
    public static class InstrumentInfoUnsubscribeRequest extends StreamingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        InstrumentInfoUnsubscribeRequest(String figi, String requestId) {
            super(requestId);
            this.figi = figi;
        }

        InstrumentInfoUnsubscribeRequest(String figi) {
            this(figi, null);
        }

        public String getFigi() {
            return figi;
        }
    }

    /**
     * Запрос на подписку на поток событий об изменениях биржевого стакана.
     */
    public static class OrderbookSubscribeRequest extends StreamingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Глубина стакана.
         */
        private final int depth;

        OrderbookSubscribeRequest(String figi, int depth, String requestId) {
            super(requestId);
            if (depth < 0  || depth > 20) throw new IllegalArgumentException("Глубина должна быть от 1 до 20");
            this.figi = figi;
            this.depth = depth;
        }

        OrderbookSubscribeRequest(String figi, int depth) {
            this(figi, depth, null);
        }

        public String getFigi() {
            return figi;
        }

        public int getDepth() {
            return depth;
        }
    }

    /**
     * Запрос на отписку от потока событий об изменениях биржевого стакана.
     */
    public static class OrderbookUnsubscribeRequest extends StreamingRequest {

        /**
         * Идентификатор инструмента.
         */
        private final String figi;

        /**
         * Глубина стакана.
         */
        private final int depth;

        OrderbookUnsubscribeRequest(String figi, int depth, String requestId) {
            super(requestId);
            if (depth < 0  || depth > 20) throw new IllegalArgumentException("Глубина должна быть от 1 до 20");
            this.figi = figi;
            this.depth = depth;
        }

        OrderbookUnsubscribeRequest(String figi, int depth) {
            this(figi, depth, null);
        }

        public String getFigi() {
            return figi;
        }

        public int getDepth() {
            return depth;
        }
    }

    public static CandleSubscribeRequest subscribeCandle(String figi, CandleInterval interval) {
        return new StreamingRequest.CandleSubscribeRequest(figi, interval);
    }

    public static CandleUnsubscribeRequest unsubscribeCandle(String figi, CandleInterval interval) {
        return new StreamingRequest.CandleUnsubscribeRequest(figi, interval);
    }

    public static OrderbookSubscribeRequest subscribeOrderbook(String figi, int depth) {
        return new StreamingRequest.OrderbookSubscribeRequest(figi, depth);
    }

    public static OrderbookUnsubscribeRequest unsubscribeOrderbook(String figi, int depth) {
        return new StreamingRequest.OrderbookUnsubscribeRequest(figi, depth);
    }

    public static InstrumentInfoSubscribeRequest subscribeInstrumentInfo(String figi) {
        return new StreamingRequest.InstrumentInfoSubscribeRequest(figi);
    }

    public static InstrumentInfoUnsubscribeRequest unsubscribeInstrumentInfo(String figi) {
        return new StreamingRequest.InstrumentInfoUnsubscribeRequest(figi);
    }
}
