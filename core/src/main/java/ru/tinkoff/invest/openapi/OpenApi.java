package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

public interface OpenApi extends AutoCloseable {
    @NotNull MarketContext getMarketContext();
    @NotNull OperationsContext getOperationsContext();
    @NotNull OrdersContext getOrdersContext();
    @NotNull PortfolioContext getPortfolioContext();
    @NotNull UserContext getUserContext();
    @NotNull StreamingContext getStreamingContext();
    boolean hasClosed();
}
