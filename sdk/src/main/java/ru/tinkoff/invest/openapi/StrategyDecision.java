package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.LimitOrder;

public abstract class StrategyDecision {

    public static class PlaceLimitOrder extends StrategyDecision {
        private final LimitOrder limitOrder;

        public PlaceLimitOrder(LimitOrder limitOrder) {
            this.limitOrder = limitOrder;
        }

        public LimitOrder getLimitOrder() {
            return limitOrder;
        }
    }

    public static class CancelOrder extends StrategyDecision {
        private final String orderId;

        public CancelOrder(String orderId) {
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

        public static Pass instance() {
            return singleton;
        }

        private Pass() {}
    }
}
