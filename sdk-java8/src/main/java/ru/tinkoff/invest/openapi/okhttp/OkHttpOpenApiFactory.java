package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class OkHttpOpenApiFactory extends OpenApiFactoryBase {

    public OkHttpOpenApiFactory(@NotNull final String token,
                                @NotNull final Logger logger) {
        super(token, logger);
    }

    @NotNull
    @Override
    public OpenApi createOpenApiClient(@NotNull final Consumer<StreamingEvent> streamingEventCallback,
                                       @NotNull final Consumer<Throwable> streamingErrorCallback) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(Duration.ofSeconds(5))
            .build();
        final String apiUrl = this.config.marketApiUrl;

        final MarketContext marketContext = new MarketContextImpl(client, apiUrl, this.authToken, this.logger);
        final OperationsContext operationsContext = new OperationsContextImpl(client, apiUrl, this.authToken, this.logger);
        final OrdersContext ordersContext = new OrdersContextImpl(client, apiUrl, this.authToken, this.logger);
        final PortfolioContext portfolioContext = new PortfolioContextImpl(client, apiUrl, this.authToken, this.logger);
        final UserContext userContext = new UserContextImpl(client, apiUrl, this.authToken, this.logger);
        final StreamingContext streamingContext = new StreamingContextImpl(client, this.config.streamingUrl, this.authToken, config.streamingParallelism, streamingEventCallback, streamingErrorCallback, this.logger);

        return new OkHttpOpenApi(
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
    public SandboxOpenApi createSandboxOpenApiClient(@NotNull Consumer<StreamingEvent> streamingEventCallback,
                                              @NotNull Consumer<Throwable> streamingErrorCallback) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(Duration.ofSeconds(5))
                .build();
        final String apiUrl = this.config.sandboxApiUrl;

        final MarketContext marketContext = new MarketContextImpl(client, apiUrl, this.authToken, this.logger);
        final OperationsContext operationsContext = new OperationsContextImpl(client, apiUrl, this.authToken, this.logger);
        final OrdersContext ordersContext = new OrdersContextImpl(client, apiUrl, this.authToken, this.logger);
        final PortfolioContext portfolioContext = new PortfolioContextImpl(client, apiUrl, this.authToken, this.logger);
        final UserContext userContext = new UserContextImpl(client, apiUrl, this.authToken, this.logger);
        final StreamingContext streamingContext = new StreamingContextImpl(client, this.config.streamingUrl, this.authToken, config.streamingParallelism, streamingEventCallback, streamingErrorCallback, this.logger);
        final SandboxContext sandboxContext = new SandboxContextImpl(client, apiUrl, this.authToken, this.logger);

        return new OkHttpSandboxOpenApi(
                marketContext,
                operationsContext,
                ordersContext,
                portfolioContext,
                userContext,
                streamingContext,
                sandboxContext
        );
    }
}
