package ru.tinkoff.invest.openapi.automata;

import ru.tinkoff.invest.openapi.model.market.CandleInterval;
import ru.tinkoff.invest.openapi.model.orders.LimitOrder;
import ru.tinkoff.invest.openapi.model.orders.Operation;
import ru.tinkoff.invest.openapi.model.streaming.StreamingRequest;

import java.math.BigDecimal;

public class InputApiSignal {

    public static class PlaceLimitOrder extends InputApiSignal {
        public final String figi;
        public final Integer lots;
        public final Operation operation;
        public final BigDecimal price;

        public PlaceLimitOrder(String figi, Integer lots, Operation operation, BigDecimal price) {
            this.figi = figi;
            this.lots = lots;
            this.operation = operation;
            this.price = price;
        }

        LimitOrder toApiEntity() {
            return new LimitOrder(figi, lots, operation, price);
        }

        @Override
        public String toString() {
            return "PlaceLimitOrder{" +
                    "figi='" + figi + '\'' +
                    ", lots=" + lots +
                    ", operation=" + operation +
                    ", price=" + price.toPlainString() +
                    '}';
        }
    }

    public static final class CancelOrder extends InputApiSignal {
        public final String figi;
        public final String orderId;

        public CancelOrder(String figi, String orderId) {
            this.figi = figi;
            this.orderId = orderId;
        }

        @Override
        public String toString() {
            return "CancelOrder{" +
                    "figi='" + figi + '\'' +
                    ", orderId='" + orderId + '\'' +
                    '}';
        }
    }

    public static final class StartCandlesStreaming extends InputApiSignal {
        public final String figi;
        public final CandleInterval interval;

        public StartCandlesStreaming(String figi, CandleInterval interval) {
            this.figi = figi;
            this.interval = interval;
        }

        StreamingRequest toApiEntity() {
            return StreamingRequest.subscribeCandle(this.figi, this.interval);
        }

        @Override
        public String toString() {
            return "StartCandlesStreaming{" +
                    "figi='" + figi + '\'' +
                    ", interval=" + interval +
                    '}';
        }
    }

    public static final class StartOrderbookStreaming extends InputApiSignal {
        public final String figi;
        public final int depth;

        public StartOrderbookStreaming(String figi, int depth) {
            this.figi = figi;
            this.depth = depth;
        }

        StreamingRequest toApiEntity() {
            return StreamingRequest.subscribeOrderbook(this.figi, this.depth);
        }

        @Override
        public String toString() {
            return "StartOrderbookStreaming{" +
                    "figi='" + figi + '\'' +
                    ", depth=" + depth +
                    '}';
        }
    }

    public static final class StartInstrumentInfoStreaming extends InputApiSignal {
        public final String figi;

        public StartInstrumentInfoStreaming(String figi) {
            this.figi = figi;
        }

        StreamingRequest toApiEntity() {
            return StreamingRequest.subscribeInstrumentInfo(this.figi);
        }

        @Override
        public String toString() {
            return "StartInstrumentInfoStreaming{" +
                    "figi='" + figi + '\'' +
                    '}';
        }
    }

    public static final class StopCandlesStreaming extends InputApiSignal {
        public final String figi;
        public final CandleInterval interval;

        public StopCandlesStreaming(String figi, CandleInterval interval) {
            this.figi = figi;
            this.interval = interval;
        }

        StreamingRequest toApiEntity() {
            return StreamingRequest.unsubscribeCandle(this.figi, this.interval);
        }
    }

    public static final class StopOrderbookStreaming extends InputApiSignal {
        public final String figi;
        public final int depth;

        public StopOrderbookStreaming(String figi, int depth) {
            this.figi = figi;
            this.depth = depth;
        }

        StreamingRequest toApiEntity() {
            return StreamingRequest.unsubscribeOrderbook(this.figi, this.depth);
        }
    }

    public static final class StopInstrumentInfoStreaming extends InputApiSignal {
        public final String figi;

        public StopInstrumentInfoStreaming(String figi) {
            this.figi = figi;
        }

        StreamingRequest toApiEntity() {
            return StreamingRequest.unsubscribeInstrumentInfo(this.figi);
        }
    }

}
