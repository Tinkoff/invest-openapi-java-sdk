package ru.tinkoff.trading.openapi.wrapper.impl;

import ru.tinkoff.trading.openapi.wrapper.WebSocketListener;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketListenerImpl implements WebSocketListener {
    private List<CharSequence> parts = new ArrayList<>();
    private CompletableFuture<?> accumulatedMessage = new CompletableFuture<>();
    private Consumer<String> messageHandler;

    public CompletionStage<?> onText(WebSocket webSocket,
                                     CharSequence data,
                                     boolean last) {
        parts.add(data);
        webSocket.request(1);
        if (last) {
            final var sb = new StringBuilder();
            for (var part : parts) {
                sb.append(part);
            }
            processWholeText(sb.toString());
            parts = new ArrayList<>();
            accumulatedMessage.complete(null);
            CompletionStage<?> cf = accumulatedMessage;
            accumulatedMessage = new CompletableFuture<>();
            return cf;
        }
        return accumulatedMessage;
    }

    private void processWholeText(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.accept(message);
        }
    }

    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }
}
