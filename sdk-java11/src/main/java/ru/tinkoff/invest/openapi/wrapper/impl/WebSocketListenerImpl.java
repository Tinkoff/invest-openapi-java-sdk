package ru.tinkoff.invest.openapi.wrapper.impl;

import ru.tinkoff.invest.openapi.wrapper.WebSocketListener;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class WebSocketListenerImpl implements WebSocketListener {
    private List<CharSequence> parts = new ArrayList<>();
    private CompletableFuture<?> accumulatedMessage = new CompletableFuture<>();
    private final SubmissionPublisher<String> streamingOnMessage;
    private final SubmissionPublisher<Void> streamingOnClose;
    private final SubmissionPublisher<Void> streamingOnError;

    public WebSocketListenerImpl() {
        streamingOnMessage = new SubmissionPublisher<>();
        streamingOnClose = new SubmissionPublisher<>();
        streamingOnError = new SubmissionPublisher<>();
    }

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
        this.streamingOnMessage.submit(message);
    }

    @Override
    public void subscribeOnMessage(Flow.Subscriber<String> subscriber) {
        this.streamingOnMessage.subscribe(subscriber);
    }

    @Override
    public void subscribeOnClose(Flow.Subscriber<Void> subscriber) {
        this.streamingOnClose.subscribe(subscriber);
    }

    @Override
    public void subscribeOnError(Flow.Subscriber<Void> subscriber) {
        this.streamingOnError.subscribe(subscriber);
    }

}
