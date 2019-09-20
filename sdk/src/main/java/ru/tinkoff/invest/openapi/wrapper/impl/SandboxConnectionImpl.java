package ru.tinkoff.invest.openapi.wrapper.impl;

import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.SandboxContext;
import ru.tinkoff.invest.openapi.wrapper.WebSocketListener;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.logging.Logger;

class SandboxConnectionImpl implements Connection<SandboxContext> {

    private final String host;
    private final String authToken;
    private final HttpClient httpClient;
    private final WebSocket webSocket;
    private final WebSocketListener listener;
    private final Logger logger;

    SandboxConnectionImpl(String host,
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
    public SandboxContext context() {
        return new SandboxContextImpl(this, logger);
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
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Session closed");
    }
}
