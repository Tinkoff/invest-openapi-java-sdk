package ru.tinkoff.invest.openapi.wrapper.impl;

import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.Context;
import ru.tinkoff.invest.openapi.wrapper.SandboxContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фабрика создания подключений к OpenAPI.
 */
public class ConnectionFactory {

    private ConnectionFactory() {}

    /**
     * Создание обычного подключения к OpenAPI.
     *
     * @param token Авторизационный токен.
     * @return Подключение с обычным контекстом.
     */
    public static CompletableFuture<Connection<Context>> connect(String token, Logger logger) {
        final var prop = extractConfig(logger);

        final var host = prop.getProperty("openapi.host");
        final var streamingHost = prop.getProperty("openapi.streaming");

        final var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)  // this is the default
                .build();
        final var authToken = "Bearer " + token;
        final var builder = httpClient.newWebSocketBuilder();
        builder.header("Authorization", authToken);
        builder.connectTimeout(Duration.ofSeconds(10));
        final var listener = new WebSocketListenerImpl();

        return builder.buildAsync(URI.create(streamingHost), listener).thenApply(
                webSocket -> new ConnectionImpl(host, authToken, httpClient, webSocket, listener, logger));
    }

    /**
     * Создание подключения к OpenAPI в режиме "песочницы".
     *
     * @param token Авторизационный токен.
     * @return Подключение с контекстом "песочницы".
     */
    public static CompletableFuture<Connection<SandboxContext>> connectSandbox(String token, Logger logger) {
        final var prop = extractConfig(logger);

        final var host = prop.getProperty("openapi.host-sandbox");
        final var streamingHost = prop.getProperty("openapi.streaming");

        final var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)  // this is the default
                .build();
        final var authToken = "Bearer " + token;
        final var builder = httpClient.newWebSocketBuilder();
        builder.header("Authorization", authToken);
        builder.connectTimeout(Duration.ofSeconds(10));
        final var listener = new WebSocketListenerImpl();

        return builder.buildAsync(URI.create(streamingHost), listener).thenApply(
                webSocket -> new SandboxConnectionImpl(host, authToken, httpClient, webSocket, listener, logger));
    }

    /**
     * Извлечение параметров конфигурации.
     *
     * @return Параметры конфигурации.
     */
    private static Properties extractConfig(Logger logger) {
        final var prop = new Properties();

        final var classLoader = ConnectionFactory.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new FileNotFoundException();
            }

            //load a properties file from class path, inside static method
            prop.load(input);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Не нашёлся файл конфигурации.", ex);
        }

        return prop;
    }

}
