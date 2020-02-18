package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class StreamingContextImpl implements StreamingContext {

    private static final TypeReference<StreamingEvent> streamingEventTypeReference =
            new TypeReference<StreamingEvent>() {
            };

    private final WebSocket[] wsClients;
    private final ArrayList<Set<StreamingRequest.ActivatingRequest>> requestsHistory;
    private final ObjectMapper mapper;
    private final Consumer<StreamingEvent> streamingEventCallback;
    private final Consumer<Throwable> streamingErrorCallback;
    private final Logger logger;
    private final OkHttpClient client;
    private final Request wsRequest;

    StreamingContextImpl(@NotNull final OkHttpClient client,
                         @NotNull final String streamingUrl,
                         @NotNull final String authToken,
                         final int streamingParallelism,
                         @NotNull final Consumer<StreamingEvent> streamingEventCallback,
                         @NotNull final Consumer<Throwable> streamingErrorCallback,
                         @NotNull final Logger logger) {
        this.streamingEventCallback = streamingEventCallback;
        this.streamingErrorCallback = streamingErrorCallback;
        this.logger = logger;
        this.client = client;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.wsClients = new WebSocket[streamingParallelism];
        this.requestsHistory = new ArrayList<>(streamingParallelism);
        this.wsRequest = new Request.Builder()
                .url(streamingUrl)
                .header("Authorization", authToken)
                .build();
        for (int i = 0; i < streamingParallelism; i++) {
            final StreamingApiListener streamingCallback = new StreamingContextImpl.StreamingApiListener(i + 1);
            this.wsClients[i] = this.client.newWebSocket(this.wsRequest, streamingCallback);
            this.requestsHistory.add(new HashSet<>());
        }
    }

    @Override
    public void sendRequest(@NotNull final StreamingRequest request) {
        int clientIndex = request.hashCode() % this.wsClients.length;
        final WebSocket wsClient = this.wsClients[clientIndex];

        try {
            final String message = mapper.writeValueAsString(request);

            final Set<StreamingRequest.ActivatingRequest> wsClientHistory = this.requestsHistory.get(clientIndex);
            wsClientHistory.removeIf(hr -> hr.onOffPairId().equals(request.onOffPairId()));
            if (request instanceof StreamingRequest.ActivatingRequest) {
                wsClientHistory.add((StreamingRequest.ActivatingRequest) request);
            }

            wsClient.send(message);
        } catch (JsonProcessingException ex) {
            logger.log(Level.SEVERE, "Что-то произошло при посыле сообщения в Streaming API клиент #" + (clientIndex + 1), ex);
            streamingErrorCallback.accept(ex);
        }
    }

    @Override
    public void close() {
        for (final WebSocket ws : this.wsClients) {
            ws.close(1000, null);
        }
        client.dispatcher().executorService().shutdown();
    }

    public void restore(@NotNull final StreamingApiListener listener) throws Exception {
        final int id = listener.id;
        final int index = listener.id - 1;
        final WebSocket webSocket = Objects.requireNonNull(this.wsClients[index]);
        logger.info("Попытка восстановления Streaming API клиента #" + id);
        webSocket.close(1000, null);

        Thread.sleep(1000);

        final WebSocket newWsClient = this.client.newWebSocket(this.wsRequest, listener);
        this.wsClients[index] = newWsClient;
        final Set<StreamingRequest.ActivatingRequest> history = this.requestsHistory.get(index);
        logger.info("У клиента #" + id + " активно " + history.size() + " подписок");

        for (final StreamingRequest.ActivatingRequest request : history) {
            final String message = mapper.writeValueAsString(request);
            newWsClient.send(message);
        }
    }

    class StreamingApiListener extends WebSocketListener implements StreamingEventHandler {

        final int id;

        StreamingApiListener(final int id) {
            this.id = id;
        }

        @Override
        public void onOpen(@NotNull final WebSocket webSocket, @NotNull final Response response) {
            super.onOpen(webSocket, response);

            logger.info("Streaming API клиент #" + id + " подключён");
        }

        @Override
        public void onMessage(@NotNull final WebSocket webSocket, @NotNull final String text) {
            super.onMessage(webSocket, text);

            try {
                final StreamingEvent event = mapper.readValue(text, streamingEventTypeReference);
                this.handleEvent(event);
            } catch (JsonProcessingException ex) {
                logger.log(Level.SEVERE, "Что-то произошло при обработке события в Streaming API клиенте #" + id, ex);
                this.handleError(ex);
            }
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);

            logger.info("Streaming API #" + id + " клиент остановлен");
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);

            logger.info("Сервер Streaming API инициировал остановку для клиента #" + id);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);

            logger.warning("Streaming API #" + id + " клиент получил байтовый тип сообщения!");
        }

        @Override
        public void onFailure(@NotNull final WebSocket webSocket,
                              @NotNull final Throwable t,
                              @Nullable final Response response) {
            super.onFailure(webSocket, t, response);

            logger.log(Level.SEVERE, "Что-то произошло в Streaming API клиенте #" + id, t);

            try {
                restore(this);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "При восстановлении Streaming API клиента #" + id + " что-то произошло", ex);
                streamingErrorCallback.accept(ex);
            }
        }

        @Override
        public void handleEvent(@NotNull final StreamingEvent event) {
            streamingEventCallback.accept(event);
        }

        @Override
        public void handleError(@NotNull final Throwable error) {
            streamingErrorCallback.accept(error);
        }
    }

}
