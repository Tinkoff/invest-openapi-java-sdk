package ru.tinkoff.invest.openapi.wrapper.impl;

import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.Context;
import ru.tinkoff.invest.openapi.wrapper.WebSocketListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConnectionImpl implements Connection<Context> {

    private final String host;
    private final String authToken;
    private final HttpClient httpClient;
    private WebSocket webSocket;
    private WebSocketListener listener;
    private final Logger logger;
    /**
     * Индикатор закрытия в "нормальном" режиме, не по инициативе сервера.
     */
    private boolean closedNormally;

    ConnectionImpl(String host,
                   String authToken,
                   HttpClient httpClient,
                   WebSocket webSocket,
                   WebSocketListener listener,
                   Logger logger) {
        this.closedNormally = false;
        this.host = host;
        this.authToken = authToken;
        this.httpClient = httpClient;
        this.webSocket = webSocket;
        this.listener = listener;
        this.logger = logger;

        this.listener.subscribeOnClose(new OnCloseSubscriber());
        this.listener.subscribeOnError(new OnErrorSubscriber());
    }

    @Override
    public Context context() {
        return new ContextImpl(this, logger);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    @Override
    public WebSocket getWebSocket() {
        return this.webSocket;
    }

    @Override
    public WebSocketListener getListener() {
        return listener;
    }

    @Override
    public void close() throws Exception {
        closedNormally = true;
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Session closed");
    }

    private class OnCloseSubscriber implements Flow.Subscriber<Void> {

        private int tryCount = 0;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Void item) {
            if (closedNormally) return;

            tryCount = tryCount == 0 ? 1 : tryCount*2;
            logger.log(
                    Level.WARNING,
                    "WebSocket-соединение закрыто по инициативе сервера. Попытка восстановать соединение #" +
                            tryCount
            );

            CompletableFuture.delayedExecutor(tryCount, TimeUnit.SECONDS).execute(() -> {
                final var builder = httpClient.newWebSocketBuilder();
                builder.header("Authorization", authToken);
                builder.connectTimeout(Duration.ofSeconds(10));
                final var newListener = new WebSocketListenerImpl();
                builder.buildAsync(URI.create(getHost()), newListener).thenApply(ws -> {
                    webSocket = ws;
                    listener = newListener;
                    if (!ws.isInputClosed() && !ws.isOutputClosed()) tryCount = 0;
                    return null;
                });
            });
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(
                    Level.SEVERE,
                    "Что-то пошло не так в подписке на стрим закрытия WebSocket-соединения.",
                    throwable
            );
        }

        @Override
        public void onComplete() {
        }
    }

    private class OnErrorSubscriber implements Flow.Subscriber<Void> {

        private int tryCount = 0;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Void item) {
            if (closedNormally) return;

            tryCount = tryCount == 0 ? 1 : tryCount*2;
            logger.log(
                    Level.WARNING,
                    "В WebSocket-соединении произошла ошибка. Попытка восстановать соединение #" + tryCount
            );

            CompletableFuture.delayedExecutor(tryCount, TimeUnit.SECONDS).execute(() -> {
                final var builder = httpClient.newWebSocketBuilder();
                builder.header("Authorization", authToken);
                builder.connectTimeout(Duration.ofSeconds(10));
                final var newListener = new WebSocketListenerImpl();
                builder.buildAsync(URI.create(getHost()), newListener).thenApply(ws -> {
                    webSocket = ws;
                    listener = newListener;
                    if (!ws.isInputClosed() && !ws.isOutputClosed()) tryCount = 0;
                    return null;
                });
            });
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(
                    Level.SEVERE,
                    "Что-то пошло не так в подписке на стрим ошибки в WebSocket-соединении.",
                    throwable
            );
        }

        @Override
        public void onComplete() {
        }
    }
}
