package ru.tinkoff.invest.openapi.wrapper.impl;

import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.Context;
import ru.tinkoff.invest.openapi.wrapper.WebSocketListener;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.logging.Logger;

class ConnectionImpl implements Connection<Context> {

    private final String host;
    private final String authToken;
    private final HttpClient httpClient;
    private final WebSocket webSocket;
    private final WebSocketListener listener;
    private final Logger logger;

    ConnectionImpl(String host,
                   String authToken,
                   HttpClient httpClient,
                   WebSocket webSocket,
                   WebSocketListener listener,
                   Logger logger) {
        this.host = host;
        this.authToken = authToken;
        this.httpClient = httpClient;
        this.webSocket = webSocket;
        this.listener = listener;
        this.logger = logger;
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
}
