package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;

public interface StreamingContext {
    void sendRequest(@NotNull StreamingRequest request);

    @NotNull
    Publisher<StreamingEvent> getEventPublisher(); 
}
