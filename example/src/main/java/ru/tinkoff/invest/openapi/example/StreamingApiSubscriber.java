package ru.tinkoff.invest.openapi.example;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.example.unicast.AsyncSubscriber;

import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

class StreamingApiSubscriber extends AsyncSubscriber<StreamingEvent> {

    private final Logger logger;

    StreamingApiSubscriber(@NotNull final Logger logger, @NotNull final Executor executor) {
        super(executor);
        this.logger = logger;
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
        logger.info("Пришло новое событие из Streaming API\n" + event);

        return true;
    }

}