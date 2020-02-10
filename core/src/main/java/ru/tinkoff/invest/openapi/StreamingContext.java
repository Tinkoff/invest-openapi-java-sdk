package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;

public interface StreamingContext {
    void sendRequest(@NotNull StreamingRequest request);

    void close();

    interface StreamingEventHandler {
        void handleEvent(@NotNull StreamingEvent event);

        void handleError(@NotNull Throwable error);
    }
}
