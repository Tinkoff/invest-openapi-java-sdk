package ru.tinkoff.invest.openapi.okhttp;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import ru.tinkoff.invest.openapi.*;

public class OkHttpOpenApi implements OpenApi {

    @NotNull
    private final MarketContextImpl marketContext;
    @NotNull
    private final OperationsContextImpl operationsContext;
    @NotNull
    private final OrdersContextImpl ordersContext;
    @NotNull
    private final PortfolioContextImpl portfolioContext;
    @NotNull
    private final UserContextImpl userContext;
    @NotNull
    private final StreamingContextImpl streamingContext;
    @NotNull
    private final OkHttpClient client;
    private boolean hasClosed;

    OkHttpOpenApi(@NotNull OkHttpClient client,
                  @NotNull final MarketContextImpl marketContext,
                  @NotNull final OperationsContextImpl operationsContext,
                  @NotNull final OrdersContextImpl ordersContext,
                  @NotNull final PortfolioContextImpl portfolioContext,
                  @NotNull final UserContextImpl userContext,
                  @NotNull final StreamingContextImpl streamingContext) {
        this.client = client;
        this.marketContext = marketContext;
        this.operationsContext = operationsContext;
        this.ordersContext = ordersContext;
        this.portfolioContext = portfolioContext;
        this.userContext = userContext;
        this.streamingContext = streamingContext;
        this.hasClosed = false;
    }

    public static OkHttpOpenApi create(
        final OkHttpClient client,
        final String apiUrl,
        final String streamingUrl,
        final int streamingParallelism,
        final String authToken,
        final Executor executor,
        final Logger logger
    ) {
        final MarketContextImpl marketContext = new MarketContextImpl(client, apiUrl, authToken, logger);
        final OperationsContextImpl operationsContext = new OperationsContextImpl(client, apiUrl, authToken, logger);
        final OrdersContextImpl ordersContext = new OrdersContextImpl(client, apiUrl, authToken, logger);
        final PortfolioContextImpl portfolioContext = new PortfolioContextImpl(client, apiUrl, authToken, logger);
        final UserContextImpl userContext = new UserContextImpl(client, apiUrl, authToken, logger);
        final StreamingContextImpl streamingContext = new StreamingContextImpl(client, streamingUrl, authToken, streamingParallelism, logger, executor);

        return new OkHttpOpenApi(
                client,
                marketContext,
                operationsContext,
                ordersContext,
                portfolioContext,
                userContext,
                streamingContext
        );
    }

    @NotNull
    @Override
    public MarketContext getMarketContext() {
        return this.marketContext;
    }

    @NotNull
    @Override
    public OperationsContext getOperationsContext() {
        return this.operationsContext;
    }

    @NotNull
    @Override
    public OrdersContext getOrdersContext() {
        return this.ordersContext;
    }

    @NotNull
    @Override
    public PortfolioContext getPortfolioContext() {
        return this.portfolioContext;
    }

    @NotNull
    @Override
    public UserContext getUserContext() {
        return this.userContext;
    }

    @NotNull
    @Override
    public StreamingContext getStreamingContext() {
        return this.streamingContext;
    }

    @Override
    public void close() throws Exception {
        if (!hasClosed) {
            if (!this.streamingContext.hasStopped()) this.streamingContext.stop();
            this.client.dispatcher().executorService().shutdown();
            this.hasClosed = true;
        }        
    }

    @Override
    public boolean hasClosed() {
        return this.hasClosed;
    }
}
