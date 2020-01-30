package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.LimitOrder;

public abstract class StrategyDecision {

    public static StrategyDecision placeLimitOrder(LimitOrder limitOrder) {
        return new PlaceLimitOrder(limitOrder);
    }

    public static StrategyDecision cancelOrder(String orderId) {
        return new CancelOrder(orderId);
    }

    public static StrategyDecision pass() {
        return Pass.instance();
    }

    public static class PlaceLimitOrder extends StrategyDecision {
        private final LimitOrder limitOrder;

        private PlaceLimitOrder(LimitOrder limitOrder) {
            this.limitOrder = limitOrder;
        }

        public LimitOrder getLimitOrder() {
            return limitOrder;
        }
    }

    public static class CancelOrder extends StrategyDecision {
        private final String orderId;

        private CancelOrder(String orderId) {
            this.orderId = orderId;
        }

        public String getOrderId() {
            return orderId;
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
