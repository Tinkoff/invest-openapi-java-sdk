package ru.tinkoff.invest.openapi.example;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.example.unicast.AsyncSubscriber;

import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

class StreamingApiSubscriber extends AsyncSubscriber<StreamingEvent> {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StreamingApiSubscriber.class);

    StreamingApiSubscriber(@NotNull final Executor executor) {
        super(executor);
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
        logger.info("Пришло новое событие из Streaming API\n" + event);

        return true;
    }

}