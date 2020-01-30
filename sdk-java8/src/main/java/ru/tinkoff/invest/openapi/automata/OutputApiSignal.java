package ru.tinkoff.invest.openapi.automata;

import ru.tinkoff.invest.openapi.model.MoneyAmount;
import ru.tinkoff.invest.openapi.model.market.CandleInterval;
import ru.tinkoff.invest.openapi.model.orders.*;
import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class OutputApiSignal {

    public final String figi;

    public OutputApiSignal(String figi) {
        this.figi = figi;
    }

    public static final class LimitOrderPlaced extends OutputApiSignal {
        public final String id;
        public final Operation operation;
        public final Status status;
        public final String rejectReason;
        public final int requestedLots;
        public final int executedLots;
        public final MoneyAmount commission;
        public final BigDecimal price;

        private LimitOrderPlaced(final String id,
                                 final Operation operation,
                                 final Status status,
                                 final String rejectReason,
                                 final int requestedLots,
                                 final int executedLots,
                                 final MoneyAmount commission,
                                 final BigDecimal price,
                                 final String figi) {
            super(figi);

            this.id = id;
            this.operation = operation;
            this.status = status;
            this.rejectReason = rejectReason;
            this.requestedLots = requestedLots;
            this.executedLots = executedLots;
            this.commission = commission;
            this.price = price;
        }

        static LimitOrderPlaced fromApiEntity(PlacedLimitOrder plo, BigDecimal price, String figi) {
            return new LimitOrderPlaced(plo.id, plo.operation, plo.status, plo.rejectReason, plo.requestedLots, plo.executedLots, plo.commission, price, figi);
        }

        @Override
        public String toString() {
            return "LimitOrderPlaced{" +
                    "id='" + id + '\'' +
                    ", operation=" + operation +
                    ", status=" + status +
                    ", rejectReason='" + rejectReason + '\'' +
                    ", requestedLots=" + requestedLots +
                    ", executedLots=" + executedLots +
                    ", commission=" + commission +
                    ", price=" + price.toPlainString() +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static final class OrderCancelled extends OutputApiSignal {
        public final String id;

        private OrderCancelled(String id, String figi) {
            super(figi);

            this.id = id;
        }

        static OrderCancelled fromApiEntity(String id, String figi) {
            return new OrderCancelled(id, figi);
        }

        @Override
        public String toString() {
            return "OrderCancelled{" +
                    "figi='" + figi + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    public static final class CandleReceived extends OutputApiSignal {
        public final BigDecimal openPrice;
        public final BigDecimal closingPrice;
        public final BigDecimal highestPrice;
        public final BigDecimal lowestPrice;
        public final BigDecimal tradingValue;
        public final ZonedDateTime dateTime;
        public final CandleInterval interval;

        private CandleReceived(BigDecimal openPrice, BigDecimal closingPrice, BigDecimal highestPrice, BigDecimal lowestPrice, BigDecimal tradingValue, ZonedDateTime dateTime, CandleInterval interval, String figi) {
            super(figi);

            this.openPrice = openPrice;
            this.closingPrice = closingPrice;
            this.highestPrice = highestPrice;
            this.lowestPrice = lowestPrice;
            this.tradingValue = tradingValue;
            this.dateTime = dateTime;
            this.interval = interval;
        }

        static CandleReceived fromApiEntity(StreamingEvent.Candle se) {
            return new CandleReceived(se.getOpenPrice(), se.getClosingPrice(), se.getHighestPrice(), se.getLowestPrice(), se.getTradingValue(), se.getDateTime(), se.getInterval(), se.getFigi());
        }

        @Override
        public String toString() {
            return "CandleReceived{" +
                    "figi='" + figi + '\'' +
                    ", openPrice=" + openPrice +
                    ", closingPrice=" + closingPrice +
                    ", highestPrice=" + highestPrice +
                    ", lowestPrice=" + lowestPrice +
                    ", tradingValue=" + tradingValue +
                    ", dateTime=" + dateTime +
                    ", interval=" + interval +
                    '}';
        }
    }

    public static final class OrderbookReceived extends OutputApiSignal {
        public final int depth;
        public final List<BigDecimal[]> bids;
        public final List<BigDecimal[]> asks;

        private OrderbookReceived(int depth, List<BigDecimal[]> bids, List<BigDecimal[]> asks, String figi) {
            super(figi);

            this.depth = depth;
            this.bids = bids;
            this.asks = asks;
        }

        static OrderbookReceived fromApiEntity(StreamingEvent.Orderbook se) {
            return new OrderbookReceived(se.getDepth(), se.getBids(), se.getAsks(), se.getFigi());
        }

        @Override
        public String toString() {
            return "OrderbookReceived{" +
                    "figi='" + figi + '\'' +
                    ", depth=" + depth +
                    '}';
        }
    }

    public static final class InstrumentInfoReceived extends OutputApiSignal {

        public final boolean canTrade;
        public final BigDecimal minPriceIncrement;
        public final int lot;
        public final BigDecimal accruedInterest;
        public final BigDecimal limitUp;
        public final BigDecimal limitDown;

        private InstrumentInfoReceived(boolean canTrade, BigDecimal minPriceIncrement, int lot, BigDecimal accruedInterest, BigDecimal limitUp, BigDecimal limitDown, String figi) {
            super(figi);

            this.canTrade = canTrade;
            this.minPriceIncrement = minPriceIncrement;
            this.lot = lot;
            this.accruedInterest = accruedInterest;
            this.limitUp = limitUp;
            this.limitDown = limitDown;
        }

        static InstrumentInfoReceived fromApiEntity(StreamingEvent.InstrumentInfo se) {
            return new InstrumentInfoReceived(se.canTrade(), se.getMinPriceIncrement(), se.getLot(), se.getAccruedInterest(), se.getLimitUp(), se.getLimitDown(), se.getFigi());
        }

        @Override
        public String toString() {
            return "InstrumentInfoReceived{" +
                    "canTrade=" + canTrade +
                    ", minPriceIncrement=" + minPriceIncrement.toPlainString() +
                    ", lot=" + lot +
                    (Objects.nonNull(accruedInterest) ? ", accruedInterest=" + accruedInterest.toPlainString() : "") +
                    (Objects.nonNull(limitUp) ? ", limitUp=" + limitDown.toPlainString() : "") +
                    (Objects.nonNull(limitDown) ? ", limitDown=" + limitUp.toPlainString() : "") +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static final class OrderStateChanged extends OutputApiSignal {
        final public String id;
        final public Status status;
        final public int executedLots;

        private OrderStateChanged(final String id, final Status status, final int executedLots, final String figi) {
            super(figi);
            this.id = id;
            this.status = status;
            this.executedLots = executedLots;
        }

        static OrderStateChanged fromApiEntity(final Order order, final String figi) {
            return new OrderStateChanged(order.id, order.status, order.executedLots, order.figi);
        }
    }

    public static final class OrderNotPlaced extends OutputApiSignal {
        public final String reason;
        public final BigDecimal amount;

        private OrderNotPlaced(final String figi, final String reason, final BigDecimal amount) {
            super(figi);

            this.reason = reason;
            this.amount = amount;
        }

        static OrderNotPlaced fromApiEntity(final String figi, final String reason, final BigDecimal amount) {
            return new OrderNotPlaced(figi, reason, amount);
        }

        @Override
        public String toString() {
            return "OrderNotPlaced{" +
                    "figi='" + figi + '\'' +
                    ", reason='" + reason + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }

    public static final class OrderExecuted extends OutputApiSignal {
        final public String id;

        private OrderExecuted(final String id, final String figi) {
            super(figi);

            this.id = id;
        }

        static OrderExecuted fromApiEntity(final String id, final String figi) {
            return new OrderExecuted(id, figi);
        }

        @Override
        public String toString() {
            return "OrderExecuted{" +
                    "figi='" + figi + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

}
