package ru.tinkoff.invest.openapi.automata;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import ru.tinkoff.invest.openapi.model.orders.Operation;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StrategyProcessor implements Processor<OutputApiSignal, InputApiSignal>, Runnable {

    private final Set<Subscription> subscriptions;
    private final Executor executor; // This is the Executor we'll use to be asynchronous, obeying rule 2.2
    private final List<Strategy> operatingStrategies;
    private final Logger logger;

    private org.reactivestreams.Subscription subscription; // Obeying rule 3.1, we make this private!
    private boolean done; // It's useful to keep track of whether this Subscriber is done or not

    public StrategyProcessor(@NotNull final Executor executor, @NotNull final Logger logger) {
        Objects.requireNonNull(executor);
        this.executor = executor;

        this.logger = logger;
        this.operatingStrategies = new LinkedList<>();
        this.subscriptions = new HashSet<>();
    }

    public void addStrategy(final Strategy strategy) {
        synchronized (operatingStrategies) {
            operatingStrategies.add(strategy);
        }
    }

    public void removeStrategy(final Strategy strategy) {
        synchronized (operatingStrategies) {
            operatingStrategies.remove(strategy);
        }
    }

    @NotNull
    public List<Strategy> strategies() {
        return new LinkedList<>(this.operatingStrategies);
    }

    @Override
    public void subscribe(Subscriber<? super InputApiSignal> s) {
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
    public final void onNext(final OutputApiSignal element) {
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

                    logger.finest("Сигналов в очереди для StrategyProcessor.");
                }
            } finally {
                on.set(false); // establishes a happens-before relationship with the beginning of the next run
                if(!inboundSignals.isEmpty()) // If we still have signals to process
                    tryScheduleToExecute(); // Then we try to schedule ourselves to execute again
            }
        }
    }

    private void emitSignal(final InputApiSignal signal) {
        logger.finest("StrategyProcessor отсылает сигнал " + signal);

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
        public final OutputApiSignal next;
        public OnNext(final OutputApiSignal next) { this.next = next; }
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
        final InputApiSignal outSignal;
        Send(final InputApiSignal outSignal) {
            this.outSignal = outSignal;
        }
    }
    private static final class Request implements PublisherSignal {
        final long n;
        Request(final long n) {
            this.n = n;
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
                logger.log(Level.SEVERE, subscription + " violated the Reactive Streams rule 3.15 by throwing an exception from cancel.", t);
            }
        }
    }

    // This method is invoked when the OnNext signals arrive
    // Returns whether more elements are desired or not, and if no more elements are desired,
    // for convenience.
    protected boolean whenNext(final OutputApiSignal element) {
        logger.finest("StrategyProcessor получил сигнал " + element);

        final List<Strategy> filtered = operatingStrategies.stream()
            .filter(s -> s.getInstrument().figi.equals(element.figi))
            .collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            final boolean waitingIsOver = element instanceof OutputApiSignal.OrderNotPlaced ||
                element instanceof OutputApiSignal.LimitOrderPlaced;

            filtered.forEach(strategy -> {
                if (waitingIsOver) strategy.internalReset();
                final TradingState newState = computeNewState(element, strategy.getCurrentState());
                if (element instanceof OutputApiSignal.LimitOrderPlaced ||
                    element instanceof OutputApiSignal.OrderCancelled ||
                    element instanceof OutputApiSignal.OrderExecuted
                ) {
                    logger.fine("Состояние после изменения статуса заявки: " + newState);
                }
                final StrategyDecision decision = strategy.handleNewState(newState);
                final InputApiSignal outputSignal = formSignalToApi(decision);
                if (Objects.nonNull(outputSignal)) {
                    logger.fine("Состояние перед отправкой решения: " + newState);
                    emitSignal(outputSignal);
                }
            });
        }

        return true;
    }

    private TradingState computeNewState(final OutputApiSignal element, final TradingState currentState) {
        if (element instanceof OutputApiSignal.CandleReceived) {
            final OutputApiSignal.CandleReceived signal = (OutputApiSignal.CandleReceived) element;
            final TradingState.Candle.CandleInterval newInterval = EntitiesAdaptor.convertApiEntityToTradingState(signal.interval);
            final TradingState.Candle newCandle = new TradingState.Candle(
                    signal.openPrice,
                    signal.closingPrice,
                    signal.highestPrice,
                    signal.lowestPrice,
                    signal.tradingValue,
                    signal.dateTime,
                    newInterval,
                    signal.figi
            );
            return currentState.withNewCandle(newCandle);
        } else if (element instanceof OutputApiSignal.InstrumentInfoReceived) {
            final OutputApiSignal.InstrumentInfoReceived signal = (OutputApiSignal.InstrumentInfoReceived) element;
            final TradingState.InstrumentInfo newInstrumentInfo = new TradingState.InstrumentInfo(
                    signal.canTrade,
                    signal.minPriceIncrement,
                    signal.lot,
                    signal.accruedInterest,
                    signal.limitUp,
                    signal.limitDown,
                    signal.figi
            );
            return currentState.withNewInstrumentInfo(newInstrumentInfo);
        } else if (element instanceof OutputApiSignal.LimitOrderPlaced) {
            final OutputApiSignal.LimitOrderPlaced signal = (OutputApiSignal.LimitOrderPlaced) element;
            final TradingState.Order newOrder = new TradingState.Order(
                    signal.id,
                    EntitiesAdaptor.convertApiEntityToTradingState(signal.operation),
                    EntitiesAdaptor.convertApiEntityToTradingState(signal.status),
                    signal.rejectReason,
                    signal.requestedLots,
                    signal.executedLots,
                    Objects.isNull(signal.commission) ? null : EntitiesAdaptor.convertApiEntityToTradingState(signal.commission),
                    signal.price,
                    signal.figi
            );
            return currentState.withPlacedOrder(newOrder);
        } else if (element instanceof OutputApiSignal.OrderbookReceived) {
            final OutputApiSignal.OrderbookReceived signal = (OutputApiSignal.OrderbookReceived) element;
            final TradingState.Orderbook newOrderbook = new TradingState.Orderbook(
                    signal.depth,
                    signal.bids.stream().map(pair -> new TradingState.Orderbook.StakeState(pair[0], pair[1].intValue())).collect(Collectors.toList()),
                    signal.asks.stream().map(pair -> new TradingState.Orderbook.StakeState(pair[0], pair[1].intValue())).collect(Collectors.toList()),
                    signal.figi
            );
            return currentState.withNewOrderbook(newOrderbook);
        } else if (element instanceof OutputApiSignal.OrderCancelled) {
            final OutputApiSignal.OrderCancelled signal = (OutputApiSignal.OrderCancelled) element;
            return currentState.withCancelledOrder(signal.id);
        } else if (element instanceof OutputApiSignal.OrderStateChanged) {
            final OutputApiSignal.OrderStateChanged signal = (OutputApiSignal.OrderStateChanged) element;
            return currentState.withChangedOrder(signal.id, EntitiesAdaptor.convertApiEntityToTradingState(signal.status), signal.executedLots);
        } else if (element instanceof OutputApiSignal.OrderExecuted) {
            final OutputApiSignal.OrderExecuted signal = (OutputApiSignal.OrderExecuted) element;
            return currentState.withExecutedOrder(signal.id);
        } else {
            return currentState;
        }
    }

    private InputApiSignal formSignalToApi(final StrategyDecision decision) {
        if (decision instanceof StrategyDecision.CancelOrder) {
            final StrategyDecision.CancelOrder concreteDecision = (StrategyDecision.CancelOrder) decision;
            return new InputApiSignal.CancelOrder(concreteDecision.figi, concreteDecision.orderId);
        } else if (decision instanceof StrategyDecision.PlaceLimitOrder) {
            final StrategyDecision.PlaceLimitOrder concreteDecision = (StrategyDecision.PlaceLimitOrder) decision;
            final Operation operation = EntitiesAdaptor.convertTradingStateToApiEntity(concreteDecision.operation);
            return new InputApiSignal.PlaceLimitOrder(concreteDecision.figi, concreteDecision.lots, operation, concreteDecision.price);
        } else {
            return null;
        }
    }

    // This method is invoked when the OnComplete signal arrives
    protected void whenComplete() { }

    // This method is invoked if the OnError signal arrives
    protected void whenError(final Throwable error) {
        logger.log(Level.SEVERE, "An error has occurred inside source of input signals: ", error);
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

    private void handleOnNext(final OutputApiSignal element) {
        if (!done) { // If we aren't already done
            if(subscription == null) { // Technically this check is not needed, since we are expecting Publishers to conform to the spec
                // Check for spec violation of 2.1 and 1.09
                logger.log(Level.SEVERE, "Someone violated the Reactive Streams rule 1.09 and 2.1 by signalling OnNext before `Subscription.request`. (no Subscription)");
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
                        logger.log(Level.SEVERE, this + " violated the Reactive Streams rule 2.13 by throwing an exception from onError.", t2);
                    }
                }
            }
        }
    }

    // Here it is important that we do not violate 2.2 and 2.3 by calling methods on the `Subscription` or `Publisher`
    private void handleOnComplete() {
        if (subscription == null) { // Technically this check is not needed, since we are expecting Publishers to conform to the spec
            // Publisher is not allowed to signal onComplete before onSubscribe according to rule 1.09
            logger.log(Level.SEVERE, "StrategyProcessor as Publisher violated the Reactive Streams rule 1.09 signalling onComplete prior to onSubscribe.");
        } else {
            done = true; // Obey rule 2.4
            whenComplete();
        }
    }

    // Here it is important that we do not violate 2.2 and 2.3 by calling methods on the `Subscription` or `Publisher`
    private void handleOnError(final Throwable error) {
        if (subscription == null) { // Technically this check is not needed, since we are expecting Publishers to conform to the spec
            // Publisher is not allowed to signal onError before onSubscribe according to rule 1.09
            logger.log(Level.SEVERE, "StrategyProcessor as Publisher violated the Reactive Streams rule 1.09 signalling onError prior to onSubscribe.");
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

        private final Subscriber<? super InputApiSignal> subscriber; // We need a reference to the `Subscriber` so we can talk to it
        private boolean cancelled = false; // This flag will track whether this `Subscription` is to be considered cancelled or not
        private long demand = 0; // Here we track the current demand, i.e. what has been requested but not yet delivered

        // This `ConcurrentLinkedQueue` will track signals that are sent to this `Subscription`, like `request` and `cancel`
        private final ConcurrentLinkedQueue<PublisherSignal> inboundSignals = new ConcurrentLinkedQueue<>();

        // We are using this `AtomicBoolean` to make sure that this `Subscription` doesn't run concurrently with itself,
        // which would violate rule 1.3 among others (no concurrent notifications).
        private final AtomicBoolean on = new AtomicBoolean(false);

        Subscription(final Subscriber<? super InputApiSignal> s) {
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
        private void doSend(final InputApiSignal s) {
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
                logger.log(Level.SEVERE, subscriber + " violated the Reactive Streams rule 2.13 by throwing an exception from onError.", t2);
            }
        }

        // What `signal` does is that it sends signals to the `Subscription` asynchronously
        private void signal(final PublisherSignal signal) {
            if (inboundSignals.offer(signal)) // No need to null-check here as ConcurrentLinkedQueue does this for us
                tryScheduleToExecute(); // Then we try to schedule it for execution, if it isn't already
        }

        @Override
        public void run() {
            if(on.get()) { // establishes a happens-before relationship with the end of the previous run
                try {
                    PublisherSignal s = inboundSignals.peek(); // We take a signal off the queue
                    if (!cancelled) { // to make sure that we follow rule 1.8, 3.6 and 3.7

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

        void consumeNext(final InputApiSignal s) {
            signal(new Send(s));
        }
    }
}
