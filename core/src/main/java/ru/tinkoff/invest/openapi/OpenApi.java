package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

public abstract class OpenApi {
    @NotNull public final MarketContext marketContext;
    @NotNull public final OperationsContext operationsContext;
    @NotNull public final OrdersContext ordersContext;
    @NotNull public final PortfolioContext portfolioContext;
    @NotNull public final UserContext userContext;
    @NotNull public final StreamingContext streamingContext;

    public OpenApi(@NotNull final MarketContext marketContext,
                   @NotNull final OperationsContext operationsContext,
                   @NotNull final OrdersContext ordersContext,
                   @NotNull final PortfolioContext portfolioContext,
                   @NotNull final UserContext userContext,
                   @NotNull final StreamingContext streamingContext) {
        this.marketContext = marketContext;
        this.operationsContext = operationsContext;
        this.ordersContext = ordersContext;
        this.portfolioContext = portfolioContext;
        this.userContext = userContext;
        this.streamingContext = streamingContext;
    }

}
