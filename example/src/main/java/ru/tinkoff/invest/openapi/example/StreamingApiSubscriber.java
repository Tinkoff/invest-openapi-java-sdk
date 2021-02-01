package ru.tinkoff.invest.openapi.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;
//import org.reactivestreams.example.unicast.AsyncSubscriber;

import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

//class StreamingApiSubscriber extends AsyncSubscriber<StreamingEvent> {
//
//    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StreamingApiSubscriber.class);
//    private final CompletableFuture<Void> stopNotifier;
//
//    StreamingApiSubscriber(final CompletableFuture<Void> stopNotifier, @NotNull final Executor executor) {
//        super(executor);
//        this.stopNotifier = stopNotifier;
//    }
//
//    @Override
//    protected boolean whenNext(final StreamingEvent event) {
//        logger.info("Пришло новое событие из Streaming API\n" + event);
//
//        return true;
//    }
//
//    @Override
//    protected void whenComplete() {
//        stopNotifier.complete(null);
//    }
//
//    @Override
//    protected void whenError(Throwable error) {
//        stopNotifier.completeExceptionally(error);
//    }
//}