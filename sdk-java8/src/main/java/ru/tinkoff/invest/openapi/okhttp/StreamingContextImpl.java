package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.Publisher;

import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

class StreamingContextImpl implements StreamingContext {

    private static final TypeReference<StreamingEvent> streamingEventTypeReference = new TypeReference<StreamingEvent>() {
    };

    private final WebSocket[] wsClients;
    private final ArrayList<Set<StreamingRequest.ActivatingRequest>> requestsHistory;
    private final ObjectMapper mapper;
    private final Logger logger;
    private final OkHttpClient client;
    private final okhttp3.Request wsRequest;
    private final StreamingEventPublisher publisher;
    
    private boolean hasStopped;

    StreamingContextImpl(@NotNull final OkHttpClient client,
                         @NotNull final String streamingUrl,
                         @NotNull final String authToken,
                         final int streamingParallelism,
                         @NotNull final Logger logger,
                         @NotNull final Executor executor) {
        this.logger = logger;
        this.publisher = new StreamingEventPublisher(executor);
        this.client = client;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.hasStopped = false;

        this.wsClients = new WebSocket[streamingParallelism];
        this.requestsHistory = new ArrayList<>(streamingParallelism);
        this.wsRequest = new okhttp3.Request.Builder().url(streamingUrl).header("Authorization", authToken).build();
        for (int i = 0; i < streamingParallelism; i++) {
            final StreamingApiListener streamingCallback = new StreamingApiListener(i + 1);
            this.wsClients[i] = this.client.newWebSocket(this.wsRequest, streamingCallback);
            this.requestsHistory.add(new HashSet<>());
        }
    }

    @Override
    public void sendRequest(@NotNull final StreamingRequest request) {
        try {
            final String message = mapper.writeValueAsString(request);

            final int clientIndex = request.hashCode() % this.wsClients.length;
            final WebSocket wsClient = this.wsClients[clientIndex];

            final Set<StreamingRequest.ActivatingRequest> wsClientHistory = this.requestsHistory.get(clientIndex);
            wsClientHistory.removeIf(hr -> hr.onOffPairId().equals(request.onOffPairId()));
            if (request instanceof StreamingRequest.ActivatingRequest) {
                wsClientHistory.add((StreamingRequest.ActivatingRequest) request);
            }

            wsClient.send(message);
        } catch (JsonProcessingException ex) {
            logger.log(Level.SEVERE, "Не удалось сериализовать сообщение в JSON", ex);
        }
    }

    static class StreamingEventPublisher implements Publisher<StreamingEvent> {

        private final List<SubscriptionImpl> subscriptions;
        private final Executor executor;

        StreamingEventPublisher(@NotNull final Executor executor) {
            this.subscriptions = new LinkedList<>();
            this.executor = executor;
        }

        @Override
        public void subscribe(Subscriber<? super StreamingEvent> s) {
            final SubscriptionImpl sub = new SubscriptionImpl(s);
            subscriptions.add(sub);
            sub.init();
        }

        interface Signal {}
        enum Cancel implements Signal {
            Instance;

            @Override
            public String toString() {
                return "Signal.Cancel";
            }
        }
        enum Subscribe implements Signal {
            Instance;

            @Override
            public String toString() {
                return "Signal.Subscribe";
            }
        }
        static final class Send implements Signal { 
            @NotNull final StreamingEvent payload;
            Send(@NotNull final StreamingEvent payload) {
                this.payload = payload;
            }
            @Override
            public String toString() {
                return "Signal.Send";
            }
        }
        static final class Request implements Signal {
            final long n;
            Request(final long n) {
                this.n = n;
            }
            @Override
            public String toString() {
                return "Signal.Request";
            }
        }

        final class SubscriptionImpl implements Subscription, Runnable {
            final Subscriber<? super StreamingEvent> subscriber; // We need a reference to the `Subscriber` so we can talk to it
            private boolean cancelled = false; // This flag will track whether this `Subscription` is to be considered cancelled or not
            private long demand = 0; // Here we track the current demand, i.e. what has been requested but not yet delivered

            SubscriptionImpl(@NotNull final Subscriber<? super StreamingEvent> subscriber) {
                this.subscriber = subscriber;
            }

            // This `ConcurrentLinkedQueue` will track signals that are sent to this `Subscription`, like `request` and `cancel`
            private final ConcurrentLinkedDeque<Signal> inboundSignals = new ConcurrentLinkedDeque<>();

            // We are using this `AtomicBoolean` to make sure that this `Subscription` doesn't run concurrently with itself,
            // which would violate rule 1.3 among others (no concurrent notifications).
            private final AtomicBoolean on = new AtomicBoolean(false);

            // This method will register inbound demand from our `Subscriber` and validate it against rule 3.9 and rule 3.17
            private void doRequest(final long n) {
                if (n < 1)
                    terminateDueTo(new IllegalArgumentException(subscriber + " violated the Reactive Streams rule 3.9 by requesting a non-positive number of elements."));
                else if (demand + n < 1) {
                    // As governed by rule 3.17, when demand overflows `Long.MAX_VALUE` we treat the signalled demand as "effectively unbounded"
                    demand = Long.MAX_VALUE;  // Here we protect from the overflow and treat it as "effectively unbounded"
                } else {
                    demand += n; // Here we record the downstream demand
                }
            }

            // This handles cancellation requests, and is idempotent, thread-safe and not synchronously performing heavy computations as specified in rule 3.5
            private void doCancel() {
                cancelled = true;
                subscriptions.remove(this);
            }

            // Instead of executing `subscriber.onSubscribe` synchronously from within `Publisher.subscribe`
            // we execute it asynchronously, this is to avoid executing the user code (`Iterable.iterator`) on the calling thread.
            // It also makes it easier to follow rule 1.9
            private void doSubscribe() {
                if (!cancelled) {
                    // Deal with setting up the subscription with the subscriber
                    try {
                        subscriber.onSubscribe(this);
                    } catch(final Throwable t) { // Due diligence to obey 2.13
                        terminateDueTo(new IllegalStateException(subscriber + " violated the Reactive Streams rule 2.13 by throwing an exception from onSubscribe.", t));
                    }
                }
            }

            // This is our behavior for producing elements downstream
            private void doSend(@NotNull final StreamingEvent next) {
                try {
                    subscriber.onNext(next); // Then we signal the next element downstream to the `Subscriber`
                    --demand;    // This makes sure that rule 1.1 is upheld (sending more than was demanded)
                } catch(final Throwable t) {
                    // We can only get here if `onNext` or `onComplete` threw, and they are not allowed to according to 2.13, so we can only cancel and log here.
                    doCancel(); // Make sure that we are cancelled, since we cannot do anything else since the `Subscriber` is faulty.
                    (new IllegalStateException(subscriber + " violated the Reactive Streams rule 2.13 by throwing an exception from onNext or onComplete.", t)).printStackTrace(System.err);
                }
            }

            // This is a helper method to ensure that we always `cancel` when we signal `onError` as per rule 1.6
            private void terminateDueTo(final Throwable t) {
                cancelled = true; // When we signal onError, the subscription must be considered as cancelled, as per rule 1.6
                try {
                    subscriber.onError(t); // Then we signal the error downstream, to the `Subscriber`
                } catch(final Throwable t2) { // If `onError` throws an exception, this is a spec violation according to rule 1.9, and all we can do is to log it.
                    (new IllegalStateException(subscriber + " violated the Reactive Streams rule 2.13 by throwing an exception from onError.", t2)).printStackTrace(System.err);
                }
            }

            // What `signal` does is that it sends signals to the `Subscription` asynchronously
            private void signal(final Signal signal) {
                if (signal instanceof Send) {
                    inboundSignals.offerLast(signal);
                } else {
                    inboundSignals.offerFirst(signal);
                }

                tryScheduleToExecute(); // Then we try to schedule it for execution, if it isn't already
            }

            // This is the main "event loop" if you so will
            @Override public final void run() {
                if(on.get()) { // establishes a happens-before relationship with the end of the previous run
                    try {
                        final Signal s = inboundSignals.peek(); // We take a signal off the queue
                        if (!cancelled) { // to make sure that we follow rule 1.8, 3.6 and 3.7
                            // Below we simply unpack the `Signal`s and invoke the corresponding methods
                            if (s instanceof Request) {
                                inboundSignals.poll();
                                doRequest(((Request)s).n);
                            } else if (s instanceof Send && demand > 0) {
                                inboundSignals.poll();
                                doSend(((Send)s).payload);
                            } else if (s == Cancel.Instance) {
                                inboundSignals.poll();
                                doCancel();
                            } else if (s == Subscribe.Instance) {
                                inboundSignals.poll();
                                doSubscribe();
                            }
                        }
                    } finally {
                        on.set(false); // establishes a happens-before relationship with the beginning of the next run
                        if(!inboundSignals.isEmpty()) // If we still have signals to process
                            tryScheduleToExecute(); // Then we try to schedule ourselves to execute again
                    }
                }
            }

            // This method makes sure that this `Subscription` is only running on one Thread at a time,
            // this is important to make sure that we follow rule 1.3
            private void tryScheduleToExecute() {
                if(on.compareAndSet(false, true)) {
                    try {
                        executor.execute(this);
                    } catch(Throwable t) { // If we can't run on the `Executor`, we need to fail gracefully
                        if (!cancelled) {
                            doCancel(); // First of all, this failure is not recoverable, so we need to follow rule 1.4 and 1.6
                            try {
                                terminateDueTo(new IllegalStateException("Publisher terminated due to unavailable Executor.", t));
                            } finally {
                                inboundSignals.clear(); // We're not going to need these anymore
                                // This subscription is cancelled by now, but letting it become schedulable again means
                                // that we can drain the inboundSignals queue if anything arrives after clearing
                                on.set(false);
                            }
                        }
                    }
                }
            }

            // Our implementation of `Subscription.request` sends a signal to the Subscription that more elements are in demand
            @Override public void request(final long n) {
                signal(new Request(n));
            }
            // Our implementation of `Subscription.cancel` sends a signal to the Subscription that the `Subscriber` is not interested in any more elements
            @Override public void cancel() {
                signal(Cancel.Instance);
            }
            // The reason for the `init` method is that we want to ensure the `SubscriptionImpl`
            // is completely constructed before it is exposed to the thread pool, therefor this
            // method is only intended to be invoked once, and immediately after the constructor has
            // finished.
            void init() {
                signal(Subscribe.Instance);
            }
        }
    }

    @Override
    @NotNull
    public Publisher<StreamingEvent> getEventPublisher() {
        return this.publisher;
    }

    public boolean hasStopped() {
        return this.hasStopped;
    }

    public synchronized void stop() {
        if (!this.hasStopped) {
            for (final WebSocket ws : this.wsClients) {
                ws.close(1000, null);
            }

            this.hasStopped = true;
        }
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

    class StreamingApiListener extends WebSocketListener {

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
                final StreamingEventPublisher.Signal signal = new StreamingEventPublisher.Send(event);
                for (final StreamingEventPublisher.SubscriptionImpl sub : publisher.subscriptions) {
                    sub.signal(signal);
                }
            } catch (JsonProcessingException ex) {
                logger.log(Level.SEVERE, "Не удалось десериализовать JSON пришедший из Streaming API", ex);
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
            }
        }
    }

}
