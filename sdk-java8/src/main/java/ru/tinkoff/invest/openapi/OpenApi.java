package ru.tinkoff.invest.openapi;

public abstract class OpenApi {
    public final MarketContext marketContext;
    public final OperationsContext operationsContext;
    public final OrdersContext ordersContext;
    public final PortfolioContext portfolioContext;
    public final StreamingContext streamingContext;

    public OpenApi(MarketContext marketContext,
                   OperationsContext operationsContext,
                   OrdersContext ordersContext,
                   PortfolioContext portfolioContext,
                   StreamingContext streamingContext) {
        this.marketContext = marketContext;
        this.operationsContext = operationsContext;
        this.ordersContext = ordersContext;
        this.portfolioContext = portfolioContext;
        this.streamingContext = streamingContext;
    }

}
