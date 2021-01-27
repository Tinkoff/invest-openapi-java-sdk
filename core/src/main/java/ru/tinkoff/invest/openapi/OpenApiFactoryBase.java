package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executor;

abstract public class OpenApiFactoryBase {

    protected final OpenApiConfig config;
    protected final String authToken;

    public OpenApiFactoryBase(@NotNull final String token) throws IOException {
        this.authToken = "Bearer " + token;

        this.config = extractConfig();
    }

    @NotNull
    abstract public OpenApi createOpenApiClient(@NotNull final Executor executor);

    @NotNull
    abstract public SandboxOpenApi createSandboxOpenApiClient(@NotNull final Executor executor);

    /**
     * Извлечение параметров конфигурации.
     *
     * @return Параметры конфигурации.
     */
    @NotNull
    protected OpenApiConfig extractConfig() throws IOException {
        final Properties prop = new Properties();

        final ClassLoader classLoader = OpenApiFactoryBase.class.getClassLoader();
        try (final InputStream input = classLoader.getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new FileNotFoundException();
            }

            //load properties file from class path, inside static method
            prop.load(input);
        }

        final String host = prop.getProperty("ru.tinkoff.invest.openapi.host");
        final String sandboxHost = prop.getProperty("ru.tinkoff.invest.openapi.host-sandbox");
        final String streamingHost = prop.getProperty("ru.tinkoff.invest.openapi.streaming");
        final int streamingParallelism = Integer.parseInt(prop.getProperty("ru.tinkoff.invest.openapi.streaming-parallelism"));

        return new OpenApiConfig(host, sandboxHost, streamingHost, streamingParallelism);
    }
}
