package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.model.streaming.StreamingRequest;

@SuppressWarnings("ReactiveStreamsPublisherImplementation")
public interface StreamingContext extends Publisher<StreamingEvent> {
    void sendRequest(@NotNull StreamingRequest request);
}
