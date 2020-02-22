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
                                final boolean sandboxMode,
                                @NotNull final Logger logger) {
        super(token, sandboxMode, logger);
    }

    @NotNull
    @Override
    public OpenApi createOpenApiClient(@NotNull final Consumer<StreamingEvent> streamingEventCallback,
                                       @NotNull final Consumer<Throwable> streamingErrorCallback) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(Duration.ofSeconds(5))
            .build();
        final String apiUrl = this.sandboxMode ? this.config.sandboxApiUrl : this.config.marketApiUrl;

        final MarketContext marketContext = new MarketContextImpl(client, apiUrl, this.authToken, this.logger);
        final OperationsContext operationsContext = new OperationsContextImpl(client, apiUrl, this.authToken, this.logger);
        final OrdersContext ordersContext = new OrdersContextImpl(client, apiUrl, this.authToken, this.logger);
        final PortfolioContext portfolioContext = new PortfolioContextImpl(client, apiUrl, this.authToken, this.logger);
        final UserContext userContext = new UserContextImpl(client, apiUrl, this.authToken, this.logger);
        final StreamingContext streamingContext = new StreamingContextImpl(client, this.config.streamingUrl, this.authToken, config.streamingParallelism, streamingEventCallback, streamingErrorCallback, this.logger);

        if (this.sandboxMode) {
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
        } else {
            return new OkHttpOpenApi(
                    marketContext,
                    operationsContext,
                    ordersContext,
                    portfolioContext,
                    userContext,
                    streamingContext
            );
        }
    }
}
