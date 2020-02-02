package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

public class OpenApiConfig {
    public final String marketApiUrl;
    public final String sandboxApiUrl;
    public final String streamingUrl;
    public final int streamingParallelism;

    public OpenApiConfig(@NotNull final String marketApiUrl,
                         @NotNull final String sandboxApiUrl,
                         @NotNull final String streamingUrl,
                         final int streamingParallelism) {
        this.marketApiUrl = marketApiUrl;
        this.sandboxApiUrl = sandboxApiUrl;
        this.streamingUrl = streamingUrl;
        this.streamingParallelism = streamingParallelism;
    }
}
