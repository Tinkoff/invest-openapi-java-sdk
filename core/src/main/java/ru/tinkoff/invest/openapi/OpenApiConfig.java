package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

public class OpenApiConfig {
    @NotNull public final String marketApiUrl;
    @NotNull public final String sandboxApiUrl;
    @NotNull public final String streamingUrl;
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
