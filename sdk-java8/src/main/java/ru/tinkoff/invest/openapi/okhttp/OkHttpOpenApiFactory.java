package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class OkHttpOpenApiFactory extends OpenApiFactoryBase {

    public OkHttpOpenApiFactory(@NotNull final String token,
                                @NotNull final Logger logger) {
        super(token, logger);
    }

    @NotNull
    @Override
    public OpenApi createOpenApiClient(@NotNull final Executor executor) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(Duration.ofSeconds(5))
            .build();
        final String apiUrl = this.config.marketApiUrl;

        return OkHttpOpenApi.create(
                client,
                apiUrl,
                config.streamingUrl,
                config.streamingParallelism,
                authToken,
                executor,
                logger
        );
    }

    @NotNull
    @Override
    public SandboxOpenApi createSandboxOpenApiClient(@NotNull final Executor executor) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(Duration.ofSeconds(5))
            .build();
        final String apiUrl = this.config.sandboxApiUrl;

        return OkHttpSandboxOpenApi.create(
                client,
                apiUrl,
                config.streamingUrl,
                config.streamingParallelism,
                authToken,
                executor,
                logger
        );
    }

}
