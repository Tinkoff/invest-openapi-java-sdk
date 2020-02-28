package ru.tinkoff.invest.openapi.okhttp;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import ru.tinkoff.invest.openapi.*;

public class OkHttpSandboxOpenApi extends OkHttpOpenApi implements SandboxOpenApi {

    @NotNull
    private final SandboxContextImpl sandboxContext;

    OkHttpSandboxOpenApi(@NotNull final OkHttpClient client,
                         @NotNull final MarketContextImpl marketContext,
                         @NotNull final OperationsContextImpl operationsContext,
                         @NotNull final OrdersContextImpl ordersContext,
                         @NotNull final PortfolioContextImpl portfolioContext,
                         @NotNull final UserContextImpl userContext,
                         @NotNull final StreamingContextImpl streamingContext,
                         @NotNull final SandboxContextImpl sandboxContext) {
        super(client, marketContext, operationsContext, ordersContext, portfolioContext, userContext, streamingContext);
        this.sandboxContext = sandboxContext;
    }

    public static OkHttpSandboxOpenApi create(
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
        final SandboxContextImpl sandboxContext = new SandboxContextImpl(client, apiUrl, authToken, logger);

        return new OkHttpSandboxOpenApi(
                client,
                marketContext,
                operationsContext,
                ordersContext,
                portfolioContext,
                userContext,
                streamingContext,
                sandboxContext
        );
    }

    @NotNull
    @Override
    public SandboxContext getSandboxContext() {
        return this.sandboxContext;
    }
}
