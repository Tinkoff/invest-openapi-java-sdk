package ru.tinkoff.invest.openapi;

public class OpenApiConfig {
    public final String marketApiUrl;
    public final String sandboxApiUrl;
    public final String streamingUrl;
    public final int streamingParallelism;

    public OpenApiConfig(String marketApiUrl, String sandboxApiUrl, String streamingUrl, int streamingParallelism) {
        this.marketApiUrl = marketApiUrl;
        this.sandboxApiUrl = sandboxApiUrl;
        this.streamingUrl = streamingUrl;
        this.streamingParallelism = streamingParallelism;
    }
}
