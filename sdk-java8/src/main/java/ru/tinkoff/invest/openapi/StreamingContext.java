package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.model.streaming.StreamingRequest;

public interface StreamingContext {
    void sendRequest(StreamingRequest request);
    void close();

    interface StreamingEventHandler {
        void handleEvent(StreamingEvent event);
        void handleError(Throwable error);
    }
}
