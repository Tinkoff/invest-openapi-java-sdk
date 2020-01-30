package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

abstract public class OpenApiFactoryBase {

    public final boolean sandboxMode;

    protected final OpenApiConfig config;
    protected final String authToken;

    public OpenApiFactoryBase(final String token,
                              final boolean sandboxMode,
                              final BiConsumer<String, Throwable> failureLogger) {
        this.authToken = "Bearer " + token;
        this.sandboxMode = sandboxMode;

        this.config = extractConfig(failureLogger);
    }

    abstract public OpenApi createOpenApiClient(Consumer<StreamingEvent> streamingEventCallback,
                                                Consumer<Throwable> streamingErrorCallback);

    /**
     * Извлечение параметров конфигурации.
     *
     * @return Параметры конфигурации.
     */
    protected static OpenApiConfig extractConfig(final BiConsumer<String, Throwable> failureLogger) {
        final Properties prop = new Properties();

        final ClassLoader classLoader = OpenApiFactoryBase.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new FileNotFoundException();
            }

            //load properties file from class path, inside static method
            prop.load(input);
        } catch (IOException ex) {
            failureLogger.accept("Не нашёлся файл конфигурации.", ex);
        }

        final String host = prop.getProperty("ru.tinkoff.invest.openapi.host");
        final String sandboxHost = prop.getProperty("ru.tinkoff.invest.openapi.host-sandbox");
        final String streamingHost = prop.getProperty("ru.tinkoff.invest.openapi.streaming");
        final int streamingParallelism = Integer.parseInt(prop.getProperty("ru.tinkoff.invest.openapi.streaming-parallelism"));

        return new OpenApiConfig(host, sandboxHost, streamingHost, streamingParallelism);
    }
}
