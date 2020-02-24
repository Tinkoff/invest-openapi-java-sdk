package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;

public class OkHttpOpenApi implements OpenApi {

    @NotNull
    private final MarketContext marketContext;
    @NotNull
    private final OperationsContext operationsContext;
    @NotNull
    private final OrdersContext ordersContext;
    @NotNull
    private final PortfolioContext portfolioContext;
    @NotNull
    private final UserContext userContext;
    @NotNull
    private final StreamingContext streamingContext;

    OkHttpOpenApi(@NotNull final MarketContext marketContext,
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

    @NotNull
    @Override
    public MarketContext getMarketContext() {
        return marketContext;
    }

    @NotNull
    @Override
    public OperationsContext getOperationsContext() {
        return operationsContext;
    }

    @NotNull
    @Override
    public OrdersContext getOrdersContext() {
        return ordersContext;
    }

    @NotNull
    @Override
    public PortfolioContext getPortfolioContext() {
        return portfolioContext;
    }

    @NotNull
    @Override
    public UserContext getUserContext() {
        return userContext;
    }

    @NotNull
    @Override
    public StreamingContext getStreamingContext() {
        return streamingContext;
    }
}
