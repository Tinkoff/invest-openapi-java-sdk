package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

public abstract class OpenApi {
    public final MarketContext marketContext;
    public final OperationsContext operationsContext;
    public final OrdersContext ordersContext;
    public final PortfolioContext portfolioContext;
    public final StreamingContext streamingContext;

    public OpenApi(@NotNull final MarketContext marketContext,
                   @NotNull final OperationsContext operationsContext,
                   @NotNull final OrdersContext ordersContext,
                   @NotNull final PortfolioContext portfolioContext,
                   @NotNull final StreamingContext streamingContext) {
        this.marketContext = marketContext;
        this.operationsContext = operationsContext;
        this.ordersContext = ordersContext;
        this.portfolioContext = portfolioContext;
        this.streamingContext = streamingContext;
    }

}
