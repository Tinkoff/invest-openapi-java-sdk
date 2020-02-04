package ru.tinkoff.invest.openapi.automata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.market.CandleInterval;
import ru.tinkoff.invest.openapi.model.orders.*;
import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class OutputApiSignal {

    @NotNull public final String figi;

    public OutputApiSignal(@NotNull final String figi) {
        this.figi = figi;
    }

    public static final class LimitOrderPlaced extends OutputApiSignal {
        @NotNull public final String id;
        @NotNull public final Operation operation;
        @NotNull public final Status status;
        public final int requestedLots;
        public final int executedLots;
        @NotNull public final BigDecimal price;

        private LimitOrderPlaced(@NotNull final String id,
                                 @NotNull final Operation operation,
                                 @NotNull final Status status,
                                 final int requestedLots,
                                 final int executedLots,
                                 @NotNull final BigDecimal price,
                                 @NotNull final String figi) {
            super(figi);

            this.id = id;
            this.operation = operation;
            this.status = status;
            this.requestedLots = requestedLots;
            this.executedLots = executedLots;
            this.price = price;
        }

        static LimitOrderPlaced fromApiEntity(@NotNull final PlacedLimitOrder plo,
                                              @NotNull final BigDecimal price,
                                              @NotNull final String figi) {
            return new LimitOrderPlaced(plo.id, plo.operation, plo.status, plo.requestedLots, plo.executedLots, price, figi);
        }

        @Override
        public String toString() {
            return "LimitOrderPlaced{" +
                    "id='" + id + '\'' +
                    ", operation=" + operation +
                    ", status=" + status +
                    ", requestedLots=" + requestedLots +
                    ", executedLots=" + executedLots +
                    ", price=" + price.toPlainString() +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static final class OrderCancelled extends OutputApiSignal {
        @NotNull public final String id;

        public OrderCancelled(@NotNull final String figi, @NotNull final String id) {
            super(figi);

            this.id = id;
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
        @NotNull public final BigDecimal openPrice;
        @NotNull public final BigDecimal closingPrice;
        @NotNull public final BigDecimal highestPrice;
        @NotNull public final BigDecimal lowestPrice;
        @NotNull public final BigDecimal tradingValue;
        @NotNull public final ZonedDateTime dateTime;
        @NotNull public final CandleInterval interval;

        private CandleReceived(@NotNull BigDecimal openPrice,
                               @NotNull BigDecimal closingPrice,
                               @NotNull BigDecimal highestPrice,
                               @NotNull BigDecimal lowestPrice,
                               @NotNull BigDecimal tradingValue,
                               @NotNull ZonedDateTime dateTime,
                               @NotNull CandleInterval interval,
                               @NotNull String figi) {
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
        @NotNull public final List<BigDecimal[]> bids;
        @NotNull public final List<BigDecimal[]> asks;

        private OrderbookReceived(final int depth,
                                  @NotNull final List<BigDecimal[]> bids,
                                  @NotNull final List<BigDecimal[]> asks,
                                  @NotNull final String figi) {
            super(figi);

            this.depth = depth;
            this.bids = bids;
            this.asks = asks;
        }

        static OrderbookReceived fromApiEntity(@NotNull final StreamingEvent.Orderbook se) {
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
        @NotNull public final BigDecimal minPriceIncrement;
        public final int lot;
        @Nullable public final BigDecimal accruedInterest;
        @Nullable public final BigDecimal limitUp;
        @Nullable public final BigDecimal limitDown;

        private InstrumentInfoReceived(final boolean canTrade,
                                       @NotNull final BigDecimal minPriceIncrement,
                                       final int lot,
                                       @Nullable final BigDecimal accruedInterest,
                                       @Nullable final BigDecimal limitUp,
                                       @Nullable final BigDecimal limitDown,
                                       @NotNull final String figi) {
            super(figi);

            this.canTrade = canTrade;
            this.minPriceIncrement = minPriceIncrement;
            this.lot = lot;
            this.accruedInterest = accruedInterest;
            this.limitUp = limitUp;
            this.limitDown = limitDown;
        }

        static InstrumentInfoReceived fromApiEntity(@NotNull final StreamingEvent.InstrumentInfo se) {
            return new InstrumentInfoReceived(se.canTrade(), se.getMinPriceIncrement(), se.getLot(), se.getAccruedInterest(), se.getLimitUp(), se.getLimitDown(), se.getFigi());
        }

        @Override
        public String toString() {
            return "InstrumentInfoReceived{" +
                    "canTrade=" + canTrade +
                    ", minPriceIncrement=" + minPriceIncrement.toPlainString() +
                    ", lot=" + lot +
                    (Objects.nonNull(accruedInterest) ? ", accruedInterest=" + accruedInterest.toPlainString() : "") +
                    (Objects.nonNull(limitUp) ? ", limitUp=" + limitUp.toPlainString() : "") +
                    (Objects.nonNull(limitDown) ? ", limitDown=" + limitDown.toPlainString() : "") +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static final class OrderStateChanged extends OutputApiSignal {
        final public String id;
        final public int executedLots;
        final public Status status;

        private OrderStateChanged(@NotNull final String figi, @NotNull final String id, @NotNull final Status status, final int executedLots) {
            super(figi);
            this.id = id;
            this.status = status;
            this.executedLots = executedLots;
        }

        static OrderStateChanged fromApiEntity(@NotNull final Order order) {
            return new OrderStateChanged(order.figi, order.id, order.status, order.executedLots);
        }
    }

    public static final class OrderNotPlaced extends OutputApiSignal {
        @NotNull
        public final String reason;

        public OrderNotPlaced(@NotNull final String figi, @NotNull final String reason) {
            super(figi);

            this.reason = reason;
        }

        @Override
        public String toString() {
            return "OrderNotPlaced{" +
                    "figi='" + figi + '\'' +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

    public static final class OrderNotCancelled extends OutputApiSignal {
        @NotNull
        public final String id;

        public OrderNotCancelled(@NotNull final String figi, @NotNull final String id) {
            super(figi);

            this.id = id;
        }

        @Override
        public String toString() {
            return "OrderNotPlaced{" +
                    "figi='" + figi + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    public static final class OrderExecuted extends OutputApiSignal {
        @NotNull final public String id;

        public OrderExecuted(@NotNull final String figi, @NotNull final String id) {
            super(figi);

            this.id = id;
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
