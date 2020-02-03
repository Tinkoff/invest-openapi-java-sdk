package ru.tinkoff.invest.openapi.automata;

import java.math.BigDecimal;

public abstract class StrategyDecision {

    public static StrategyDecision placeLimitOrder(String figi, int lots, TradingState.Order.Type operation, BigDecimal price) {
        return new PlaceLimitOrder(figi, lots, operation, price);
    }

    public static StrategyDecision cancelOrder(String figi, String orderId) {
        return new CancelOrder(figi, orderId);
    }

    public static StrategyDecision pass() {
        return Pass.instance();
    }

    public static class PlaceLimitOrder extends StrategyDecision {
        public final String figi;
        public final Integer lots;
        public final TradingState.Order.Type operation;
        public final BigDecimal price;

        private PlaceLimitOrder(String figi, Integer lots, TradingState.Order.Type operation, BigDecimal price) {
            this.figi = figi;
            this.lots = lots;
            this.operation = operation;
            this.price = price;
        }
    }

    public static class CancelOrder extends StrategyDecision {
        public final String figi;
        public final String orderId;

        private CancelOrder(String figi, String orderId) {
            this.figi = figi;
            this.orderId = orderId;
        }
    }

    public static class Pass extends StrategyDecision {
        private static final Pass singleton;

        static {
            singleton = new Pass();
        }

        private static Pass instance() {
            return singleton;
        }

        private Pass() {}
    }
}
