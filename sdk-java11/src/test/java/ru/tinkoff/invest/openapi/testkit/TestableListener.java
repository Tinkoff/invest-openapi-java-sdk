package ru.tinkoff.invest.openapi.testkit;

import ru.tinkoff.invest.openapi.wrapper.WebSocketListener;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class TestableListener implements WebSocketListener {

    private final SubmissionPublisher<String> streamingOnMessage = new SubmissionPublisher<>();

    public void receiveText(CharSequence data) {
        this.streamingOnMessage.submit(data.toString());
    }

    @Override
    public void subscribeOnMessage(Flow.Subscriber<String> subscriber) {
        this.streamingOnMessage.subscribe(subscriber);
    }

    @Override
    public void subscribeOnClose(Flow.Subscriber<Void> subscriber) {

    }

    @Override
    public void subscribeOnError(Flow.Subscriber<Void> subscriber) {

    }

    @Override
    public void onOpen(WebSocket webSocket) {

    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {

    }
}
