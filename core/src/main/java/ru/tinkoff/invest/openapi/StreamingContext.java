package ru.tinkoff.invest.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.model.streaming.StreamingRequest;

import java.io.Closeable;

public interface StreamingContext extends Closeable {
    void sendRequest(@NotNull StreamingRequest request) throws JsonProcessingException;

    @NotNull
    Publisher<StreamingEvent> getEventPublisher(); 
}
