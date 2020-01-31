package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.OkHttpClient;
import ru.tinkoff.invest.openapi.*;
import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OkHttpOpenApiFactory extends OpenApiFactoryBase {

    private final Logger logger;

    public OkHttpOpenApiFactory(final String token, final boolean sandboxMode, Logger logger) {
        super(token, sandboxMode, (msg, err) -> logger.log(Level.SEVERE, msg, err));

        this.logger = logger;
    }

    @Override
    public OpenApi createOpenApiClient(Consumer<StreamingEvent> streamingEventCallback, Consumer<Throwable> streamingErrorCallback) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(Duration.ofSeconds(5))
            .build();
        final String apiUrl = this.sandboxMode ? this.config.sandboxApiUrl : this.config.marketApiUrl;

        final MarketContext marketContext = new MarketContextImpl(client, apiUrl, this.authToken, this.logger);
        final OperationsContext operationsContext = new OperationsContextImpl(client, apiUrl, this.authToken, this.logger);
        final OrdersContext ordersContext = new OrdersContextImpl(client, apiUrl, this.authToken, this.logger);
        final PortfolioContext portfolioContext = new PortfolioContextImpl(client, apiUrl, this.authToken, this.logger);
        final StreamingContext streamingContext = new StreamingContextImpl(client, this.config.streamingUrl, this.authToken, config.streamingParallelism, streamingEventCallback, streamingErrorCallback, this.logger);

        if (this.sandboxMode) {
            final SandboxContext sandboxContext = new SandboxContextImpl(client, apiUrl, this.authToken, this.logger);

            return new OkHttpSandboxOpenApi(
                    marketContext,
                    operationsContext,
                    ordersContext,
                    portfolioContext,
                    streamingContext,
                    sandboxContext
            );
        } else {
            return new OkHttpOpenApi(
                    marketContext,
                    operationsContext,
                    ordersContext,
                    portfolioContext,
                    streamingContext
            );
        }
    }
}
