package ru.tinkoff.invest.openapi.automata;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OpenApiFactoryBase;
import ru.tinkoff.invest.openapi.exceptions.NotEnoughBalanceException;
import ru.tinkoff.invest.openapi.model.streaming.StreamingEvent;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReactiveApi implements Processor<InputApiSignal, OutputApiSignal>, Runnable {

    @NotNull public final OpenApi api;

    private final Set<Subscription> subscriptions;
    private final Executor executor; // This is the Executor we'll use to be asynchronous, obeying rule 2.2
    private final Logger logger;

    private org.reactivestreams.Subscription subscription; // Obeying rule 3.1, we make this private!
    private boolean done; // It's useful to keep track of whether this Subscriber is done or not

    public ReactiveApi(@NotNull final Executor executor,
                       @NotNull final OpenApiFactoryBase factory,
                       @NotNull final Logger logger) {
        Objects.requireNonNull(executor);
        this.executor = executor;
        final OpenApi api = factory.createOpenApiClient(this::streamingConsumer, this::onError);
        Objects.requireNonNull(api);
        this.api = api;

        this.logger = logger;
        this.subscriptions = new HashSet<>();
    }

    @Override
    public void subscribe(final Subscriber<? super OutputApiSignal> s) {
        // As per 2.13, this method must return normally (i.e. not throw)
        final Subscription newSub = new Subscription(s);
        newSub.init();
        subscriptions.add(newSub);
    }

    // We implement the OnX methods on `Subscriber` to send Signals that we will process asynchronously, but only one at a time

    @Override
    public final void onSubscribe(final org.reactivestreams.Subscription s) {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `Subscription` is `null`
        Objects.requireNonNull(s);

        signal(new OnSubscribe(s));
    }

    @Override
    public final void onNext(final InputApiSignal element) {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `element` is `null`
        Objects.requireNonNull(element);

        signal(new OnNext(element));
    }

    @Override
    public final void onError(final Throwable t) {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `Throwable` is `null`
        Objects.requireNonNull(t);

        signal(new OnError(t));
    }

    @Override
    public final void onComplete() {
        signal(OnComplete.Instance);
    }

    @Override
    public final void run() {
        if(on.get()) { // establishes a happens-before relationship with the end of the previous run
            try {
                if (!done) { // If we're done, we shouldn't process any more signals, obeying rule 2.8
                    final SubscriberSignal s = inboundSignals.poll(); // We take a signal off the queue
                    // Below we simply unpack the `Signal`s and invoke the corresponding methods
                    if (s instanceof OnNext)
                        handleOnNext(((OnNext)s).next);
                    else if (s instanceof OnSubscribe)
                        handleOnSubscribe(((OnSubscribe)s).subscription);
                    else if (s instanceof OnError) // We are always able to handle OnError, obeying rule 2.10
                        handleOnError(((OnError)s).error);
                    else if (s == OnComplete.Instance) // We are always able to handle OnComplete, obeying rule 2.9
                        handleOnComplete();

                    logger.finest("Сигналов в очереди для ReactiveApi.");
                }
            } finally {
                on.set(false); // establishes a happens-before relationship with the beginning of the next run
                if(!inboundSignals.isEmpty()) // If we still have signals to process
                    tryScheduleToExecute(); // Then we try to schedule ourselves to execute again
            }
        }
    }

    private void emitSignal(final OutputApiSignal signal) {
        for (final Subscription s : this.subscriptions) {
            s.consumeNext(signal);
        }
    }

    // Signal represents the asynchronous protocol between the Publisher and Subscriber
    private interface SubscriberSignal {}
    private enum OnComplete implements SubscriberSignal { Instance }
    private static class OnError implements SubscriberSignal {
        public final Throwable error;
        public OnError(final Throwable error) { this.error = error; }
    }
    private static class OnNext implements SubscriberSignal {
        public final InputApiSignal next;
        public OnNext(final InputApiSignal next) { this.next = next; }
    }
    private static class OnSubscribe implements SubscriberSignal {
        public final org.reactivestreams.Subscription subscription;
        public OnSubscribe(final org.reactivestreams.Subscription subscription) { this.subscription = subscription; }
    }

    // These represent the protocol of the `ReactiveOpenApis` Subscription
    private interface PublisherSignal {}
    private enum Cancel implements PublisherSignal { Instance }
    private enum Subscribe implements PublisherSignal { Instance }
    private static final class Send implements PublisherSignal {
        final OutputApiSignal outSignal;
        Send(final OutputApiSignal outSignal) {
            this.outSignal = outSignal;
        }
    }
    private static final class Request implements PublisherSignal {
        final long n;
        Request(final long n) {
            this.n = n;
        }
    }


    private void streamingConsumer(StreamingEvent se) {
        if (se instanceof StreamingEvent.Candle) {
            emitSignal(OutputApiSignal.CandleReceived.fromApiEntity((StreamingEvent.Candle) se));
        } else if (se instanceof StreamingEvent.Orderbook) {
            emitSignal(OutputApiSignal.OrderbookReceived.fromApiEntity((StreamingEvent.Orderbook) se));
        } else if (se instanceof StreamingEvent.InstrumentInfo) {
            emitSignal(OutputApiSignal.InstrumentInfoReceived.fromApiEntity((StreamingEvent.InstrumentInfo) se));
        } else {
            this.onError(new Exception(((StreamingEvent.Error) se).geError()));
        }
    }

    // Showcases a convenience method to idempotently marking the Subscriber as "done", so we don't want to process more elements
    // therefor we also need to cancel our `Subscription`.
    private void done() {
        //On this line we could add a guard against `!done`, but since rule 3.7 says that `Subscription.cancel()` is idempotent, we don't need to.
        done = true; // If `whenNext` throws an exception, let's consider ourselves done (not accepting more elements)
        if (subscription != null) { // If we are bailing out before we got a `Subscription` there's little need for cancelling it.
            try {
                subscription.cancel(); // Cancel the subscription
            } catch(final Throwable t) {
                //Subscription.cancel is not allowed to throw an exception, according to rule 3.15
                logger.log(Level.SEVERE, "ReactiveApi violated the Reactive Streams rule 3.15 by throwing an exception from cancel.", t);
            }
        }
    }

    // This method is invoked when the OnNext signals arrive
    // Returns whether more elements are desired or not, and if no more elements are desired,
    // for convenience.
    protected boolean whenNext(final InputApiSignal element) {
        logger.finest("ReactiveApi получил сигнал " + element);

        if (element instanceof InputApiSignal.StartCandlesStreaming) {
            final InputApiSignal.StartCandlesStreaming concreteSignal =
                    (InputApiSignal.StartCandlesStreaming) element;

            api.streamingContext.sendRequest(concreteSignal.toApiEntity());
        } else if (element instanceof InputApiSignal.StartOrderbookStreaming) {
            final InputApiSignal.StartOrderbookStreaming concreteSignal =
                    (InputApiSignal.StartOrderbookStreaming) element;

            api.streamingContext.sendRequest(concreteSignal.toApiEntity());
        } else if (element instanceof InputApiSignal.StartInstrumentInfoStreaming) {
            final InputApiSignal.StartInstrumentInfoStreaming concreteSignal =
                    (InputApiSignal.StartInstrumentInfoStreaming) element;

            api.streamingContext.sendRequest(concreteSignal.toApiEntity());
        } else if (element instanceof InputApiSignal.StopCandlesStreaming) {
            final InputApiSignal.StopCandlesStreaming concreteSignal =
                    (InputApiSignal.StopCandlesStreaming) element;

            api.streamingContext.sendRequest(concreteSignal.toApiEntity());
        } else if (element instanceof InputApiSignal.StopOrderbookStreaming) {
            final InputApiSignal.StopOrderbookStreaming concreteSignal =
                    (InputApiSignal.StopOrderbookStreaming) element;

            api.streamingContext.sendRequest(concreteSignal.toApiEntity());
        } else if (element instanceof InputApiSignal.StopInstrumentInfoStreaming) {
            final InputApiSignal.StopInstrumentInfoStreaming concreteSignal =
                    (InputApiSignal.StopInstrumentInfoStreaming) element;

            api.streamingContext.sendRequest(concreteSignal.toApiEntity());
        } else if (element instanceof InputApiSignal.PlaceLimitOrder) {
            final InputApiSignal.PlaceLimitOrder concreteSignal =
                    (InputApiSignal.PlaceLimitOrder) element;

            api.ordersContext.placeLimitOrder(
                    concreteSignal.toApiEntity(),
                    plo -> {
                        final OutputApiSignal.LimitOrderPlaced data = OutputApiSignal.LimitOrderPlaced.fromApiEntity(plo, concreteSignal.price, concreteSignal.figi);
                        emitSignal(data);
                        executor.execute(new OrderMonitor(data, logger));
                    },
                    ex -> {
                        if (ex instanceof NotEnoughBalanceException) {
                            final BigDecimal amount = concreteSignal.price.multiply(BigDecimal.valueOf(concreteSignal.lots));
                            emitSignal(OutputApiSignal.OrderNotPlaced.fromApiEntity(concreteSignal.figi, ex.getMessage(), amount));
                        } else {
                            this.onError(ex);
                        }
                    });
        } else {
            final InputApiSignal.CancelOrder concreteSignal =
                    (InputApiSignal.CancelOrder) element;

            api.ordersContext.cancelOrder(
                    concreteSignal.orderId,
                    empty -> emitSignal(OutputApiSignal.OrderCancelled.fromApiEntity(concreteSignal.orderId, concreteSignal.figi)),
                    this::onError);
        }

        return true;
    }

    // This method is invoked when the OnComplete signal arrives
    protected void whenComplete() {
        logger.warning("Reactive Api больше не получает сигналов!");
    }

    // This method is invoked if the OnError signal arrives
    protected void whenError(final Throwable error) {
        logger.log(Level.SEVERE, "Что-то прозошло с ReactiveApi!", error);
    }

    private void handleOnSubscribe(final org.reactivestreams.Subscription s) {
        if (s == null) {
            // Getting a null `Subscription` here is not valid so lets just ignore it.
            logger.warning("Невалидная ссылка на подписку!");
        } else if (subscription != null) { // If someone has made a mistake and added this Subscriber multiple times, let's handle it gracefully
            try {
                s.cancel(); // Cancel the additional subscription to follow rule 2.5
            } catch(final Throwable t) {
                //Subscription.cancel is not allowed to throw an exception, according to rule 3.15
                logger.log(Level.SEVERE, s + " violated the Reactive Streams rule 3.15 by throwing an exception from cancel.", t);
            }
        } else {
            // We have to assign it locally before we use it, if we want to be a synchronous `Subscriber`
            // Because according to rule 3.10, the Subscription is allowed to call `onNext` synchronously from within `request`
            subscription = s;
            try {
                // If we want elements, according to rule 2.1 we need to call `request`
                // And, according to rule 3.2 we are allowed to call this synchronously from within the `onSubscribe` method
                s.request(1); // Our Subscriber is unbuffered and modest, it requests one element at a time
            } catch(final Throwable t) {
                // Subscription.request is not allowed to throw according to rule 3.16
                logger.log(Level.SEVERE, s + " violated the Reactive Streams rule 3.16 by throwing an exception from request.", t);
            }
        }
    }

    private void handleOnNext(final InputApiSignal element) {
        if (!done) { // If we aren't already done
            if(subscription == null) { // Technically this check is not needed, since we are expecting Publishers to conform to the spec
                // Check for spec violation of 2.1 and 1.09
                logger.log(Level.SEVERE, "Someone violated the Reactive Streams rule 1.09 and 2.1 by signalling OnNext before `Subscription.request`. (no Subscription).");
            } else {
                try {
                    if (whenNext(element)) {
                        try {
                            subscription.request(1); // Our Subscriber is unbuffered and modest, it requests one element at a time
                        } catch(final Throwable t) {
                            // Subscription.request is not allowed to throw according to rule 3.16
                            logger.log(Level.SEVERE, subscription + " violated the Reactive Streams rule 3.16 by throwing an exception from request.", t);
                        }
                    } else {
                        done(); // This is legal according to rule 2.6
                    }
                } catch(final Throwable t) {
                    done();
                    try {
                        onError(t);
                    } catch(final Throwable t2) {
                        //Subscriber.onError is not allowed to throw an exception, according to rule 2.13
                        logger.log(Level.SEVERE, "ReactiveApi violated the Reactive Streams rule 2.13 by throwing an exception from onError.", t2);
                    }
                }
            }
        }
    }

    // Here it is important that we do not violate 2.2 and 2.3 by calling methods on the `Subscription` or `Publisher`
    private void handleOnComplete() {
        if (subscription == null) { // Technically this check is not needed, since we are expecting Publishers to conform to the spec
            // Publisher is not allowed to signal onComplete before onSubscribe according to rule 1.09
            logger.severe("ReactiveApi (as Publisher) violated the Reactive Streams rule 1.09 signalling onComplete prior to onSubscribe.");
        } else {
            done = true; // Obey rule 2.4
            whenComplete();
        }
    }

    // Here it is important that we do not violate 2.2 and 2.3 by calling methods on the `Subscription` or `Publisher`
    private void handleOnError(final Throwable error) {
        if (subscription == null) { // Technically this check is not needed, since we are expecting Publishers to conform to the spec
            // Publisher is not allowed to signal onError before onSubscribe according to rule 1.09
            logger.severe("ReactiveApi (as Publisher) violated the Reactive Streams rule 1.09 signalling onError prior to onSubscribe.");
        } else {
            done = true; // Obey rule 2.4
            whenError(error);
        }
    }

    // This `ConcurrentLinkedQueue` will track signals that are sent to this `Subscriber`, like `OnComplete` and `OnNext` ,
    // and obeying rule 2.11
    private final ConcurrentLinkedQueue<SubscriberSignal> inboundSignals = new ConcurrentLinkedQueue<>();

    // We are using this `AtomicBoolean` to make sure that this `Subscriber` doesn't run concurrently with itself,
    // obeying rule 2.7 and 2.11
    private final AtomicBoolean on = new AtomicBoolean(false);

    // What `signal` does is that it sends signals to the `Subscription` asynchronously
    private void signal(final SubscriberSignal signal) {
        if (inboundSignals.offer(signal)) // No need to null-check here as ConcurrentLinkedQueue does this for us
            tryScheduleToExecute(); // Then we try to schedule it for execution, if it isn't already
    }

    // This method makes sure that this `Subscriber` is only executing on one Thread at a time
    private void tryScheduleToExecute() {
        if(on.compareAndSet(false, true)) {
            try {
                executor.execute(this);
            } catch(Throwable t) { // If we can't run on the `Executor`, we need to fail gracefully and not violate rule 2.13
                if (!done) {
                    try {
                        done(); // First of all, this failure is not recoverable, so we need to cancel our subscription
                    } finally {
                        inboundSignals.clear(); // We're not going to need these anymore
                        // This subscription is cancelled by now, but letting the Subscriber become schedulable again means
                        // that we can drain the inboundSignals queue if anything arrives after clearing
                        on.set(false);
                    }
                }
            }
        }
    }

    final class Subscription implements org.reactivestreams.Subscription, Runnable {

        private final Subscriber<? super OutputApiSignal> subscriber; // We need a reference to the `Subscriber` so we can talk to it
        private boolean cancelled = false; // This flag will track whether this `Subscription` is to be considered cancelled or not
        private long demand = 0; // Here we track the current demand, i.e. what has been requested but not yet delivered

        // This `ConcurrentLinkedQueue` will track signals that are sent to this `Subscription`, like `request` and `cancel`
        private final ConcurrentLinkedDeque<PublisherSignal> inboundSignals = new ConcurrentLinkedDeque<>();

        // We are using this `AtomicBoolean` to make sure that this `Subscription` doesn't run concurrently with itself,
        // which would violate rule 1.3 among others (no concurrent notifications).
        private final AtomicBoolean on = new AtomicBoolean(false);

        Subscription(final Subscriber<? super OutputApiSignal> s) {
            // As per rule 1.09, we need to throw a `java.lang.NullPointerException` if the `Subscriber` is `null`
            Objects.requireNonNull(s);
            subscriber = s;
        }

        // This method will register inbound demand from our `Subscriber` and validate it against rule 3.9 and rule 3.17
        private void doRequest(final long n) {
            if (n < 1)
                terminateDueTo(new IllegalArgumentException(subscriber + " violated the Reactive Streams rule 3.9 by requesting a non-positive number of elements."));
            else if (demand + n < 1) {
                // As governed by rule 3.17, when demand overflows `Long.MAX_VALUE` we treat the signalled demand as "effectively unbounded"
                demand = Long.MAX_VALUE;  // Here we protect from the overflow and treat it as "effectively unbounded"
                tryScheduleToExecute();
            } else {
                demand += n; // Here we record the downstream demand
                tryScheduleToExecute();
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
        private void doSend(final OutputApiSignal s) {
            logger.finest("ReactiveApi отсылает сигнал " + s);

            try {
                subscriber.onNext(s); // Then we signal the next element downstream to the `Subscriber`
                --demand;
            } catch(final Throwable t) {
                // We can only get here if `onNext` or `onComplete` threw, and they are not allowed to according to 2.13, so we can only cancel and log here.
                doCancel(); // Make sure that we are cancelled, since we cannot do anything else since the `Subscriber` is faulty.
                logger.log(Level.SEVERE, subscriber + " violated the Reactive Streams rule 2.13 by throwing an exception from onNext or onComplete.", t);
            }
        }

        // This is a helper method to ensure that we always `cancel` when we signal `onError` as per rule 1.6
        private void terminateDueTo(final Throwable t) {
            cancelled = true; // When we signal onError, the subscription must be considered as cancelled, as per rule 1.6
            try {
                subscriber.onError(t); // Then we signal the error downstream, to the `Subscriber`
            } catch(final Throwable t2) { // If `onError` throws an exception, this is a spec violation according to rule 1.9, and all we can do is to log it.
                logger.log(Level.SEVERE, subscriber + " violated the Reactive Streams rule 2.13 by throwing an exception from onError.", t);
            }
        }

        // What `signal` does is that it sends signals to the `Subscription` asynchronously
        private void signal(final PublisherSignal signal) {
            if (signal instanceof Send) {
                if (inboundSignals.offerLast(signal)) // No need to null-check here as ConcurrentLinkedQueue does this for us
                    tryScheduleToExecute(); // Then we try to schedule it for execution, if it isn't already
            } else {
                if (inboundSignals.offerFirst(signal))
                    tryScheduleToExecute();
            }
        }

        @Override
        public void run() {
            if(on.get()) { // establishes a happens-before relationship with the end of the previous run
                try {
                    if (!cancelled) { // to make sure that we follow rule 1.8, 3.6 and 3.7
                        final PublisherSignal s = inboundSignals.peek(); // We take a signal off the queue

                        // Below we simply unpack the `Signal`s and invoke the corresponding methods
                        if (s instanceof Request) {
                            inboundSignals.poll();
                            doRequest(((Request) s).n);
                        } else if (s instanceof Send) {
                            if (demand > 0) {
                                inboundSignals.poll();
                                doSend(((Send) s).outSignal);
                            }
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
                    subscriptions.remove(this);
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

        void consumeNext(final OutputApiSignal s) {
            signal(new Send(s));
        }
    }

    final class OrderMonitor implements Runnable {
        private final OutputApiSignal.LimitOrderPlaced order;
        private final Logger logger;

        OrderMonitor(final OutputApiSignal.LimitOrderPlaced order, final Logger logger) {
            this.order = order;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                api.ordersContext.getOrders(
                        orders -> {
                            final boolean executed = orders.stream().noneMatch(o -> o.id.equals(this.order.id));
                            if (executed) {
                                emitSignal(OutputApiSignal.OrderExecuted.fromApiEntity(this.order.id, this.order.figi));
                            } else {
                                executor.execute(this);
                            }
                        },
                        error -> logger.log(Level.SEVERE, "Не удалось получить заявки.", error)
                );
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "При мониторинге заявки что-то произошло.", ex);
                executor.execute(this);
            }
        }
    }
}
