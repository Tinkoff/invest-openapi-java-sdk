package ru.tinkoff.invest.openapi.okhttp;

import java.util.concurrent.Executor;

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
        final Executor executor
    ) {
        final MarketContextImpl marketContext = new MarketContextImpl(client, apiUrl, authToken);
        final OperationsContextImpl operationsContext = new OperationsContextImpl(client, apiUrl, authToken);
        final OrdersContextImpl ordersContext = new OrdersContextImpl(client, apiUrl, authToken);
        final PortfolioContextImpl portfolioContext = new PortfolioContextImpl(client, apiUrl, authToken);
        final UserContextImpl userContext = new UserContextImpl(client, apiUrl, authToken);
        final StreamingContextImpl streamingContext = new StreamingContextImpl(client, streamingUrl, authToken, streamingParallelism, executor);
        final SandboxContextImpl sandboxContext = new SandboxContextImpl(client, apiUrl, authToken);

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
