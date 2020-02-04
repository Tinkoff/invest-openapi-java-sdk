package ru.tinkoff.invest.openapi.automata;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Объект представляющий для стратегии информацию о ситуации на рынке.
 */
public class TradingState {

    @Nullable
    public final Orderbook orderbook;
    @Nullable
    public final Candle candle;
    @Nullable
    public final InstrumentInfo instrumentInfo;
    @NotNull
    public final List<Order> orders;
    @NotNull
    public final Map<Currency, Position> currencies;
    @NotNull
    public final Map<String, Position> positions;
    @NotNull
    public final List<Operation> operations;
    public boolean waitingForPlacingOrder;

    private final Currency instrumentCurrency;

    public static TradingState init(@NotNull final List<Order> orders,
                                    @NotNull final Map<Currency, Position> currencies,
                                    @NotNull final Map<String, Position> positions,
                                    @NotNull final Currency instrumentCurrency) {
        return new TradingState(
                null,
                null,
                null,
                orders,
                currencies,
                positions,
                instrumentCurrency,
                new LinkedList<>(),
                false
        );
    }

    private TradingState(@Nullable final Orderbook orderbook,
                         @Nullable final Candle candle,
                         @Nullable final InstrumentInfo instrumentInfo,
                         @NotNull final List<Order> orders,
                         @NotNull final Map<Currency, Position> currencies,
                         @NotNull final Map<String, Position> positions,
                         @NotNull final Currency instrumentCurrency,
                         @NotNull final List<Operation> operations,
                         final boolean waitingForPlacingOrder) {
        this.orderbook = orderbook;
        this.candle = candle;
        this.instrumentInfo = instrumentInfo;
        this.orders = orders;
        this.currencies = currencies;
        this.positions = positions;
        this.instrumentCurrency = instrumentCurrency;
        this.operations = operations;
        this.waitingForPlacingOrder = waitingForPlacingOrder;
    }

    @NotNull
    public TradingState withNewCandle(@NotNull final Candle candle) {
        return new TradingState(
                this.orderbook,
                candle,
                this.instrumentInfo,
                this.orders,
                this.currencies,
                this.positions,
                this.instrumentCurrency,
                this.operations,
                this.waitingForPlacingOrder
        );
    }

    @NotNull
    public TradingState withNewOrderbook(@NotNull final Orderbook orderbook) {
        return new TradingState(
                orderbook,
                this.candle,
                this.instrumentInfo,
                this.orders,
                this.currencies,
                this.positions,
                this.instrumentCurrency,
                this.operations,
                this.waitingForPlacingOrder
        );
    }

    @NotNull
    public TradingState withNewInstrumentInfo(@NotNull final InstrumentInfo instrumentInfo) {
        final List<Order> orders;
        final Map<Currency, Position> currencies;
        final Map<String, Position> positions;
        final boolean currentCanTrade = Objects.nonNull(this.instrumentInfo) && this.instrumentInfo.canTrade;
        if (instrumentInfo.canTrade != currentCanTrade && !instrumentInfo.canTrade) {
            orders = new LinkedList<>();

            final BigDecimal buyOrdersAmount = this.orders.stream()
                    .filter(o -> o.operation == Order.Type.Buy)
                    .reduce(BigDecimal.ZERO,
                            (acc, order) -> order.price.multiply(BigDecimal.valueOf(order.executedLots * instrumentInfo.lot)),
                            BigDecimal::add);
            currencies = this.currencies.entrySet().stream()
                    .peek(entry -> {
                        if (this.instrumentCurrency == entry.getKey()) {
                            final Position newValue = entry.getValue()
                                    .withBlocked(entry.getValue().blocked.subtract(buyOrdersAmount))
                                    .withBalance(entry.getValue().balance.add(buyOrdersAmount));
                            entry.setValue(newValue);
                        }
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            final BigDecimal sellOrdersValue = this.orders.stream()
                    .filter(o -> o.operation == Order.Type.Buy)
                    .reduce(BigDecimal.ZERO,
                            (acc, order) -> BigDecimal.valueOf(order.executedLots * instrumentInfo.lot),
                            BigDecimal::add);
            positions = this.positions.entrySet().stream()
                    .peek(entry -> {
                        if (this.instrumentInfo.figi.equals(entry.getKey())) {
                            final Position newValue = entry.getValue()
                                    .withBlocked(entry.getValue().blocked.subtract(sellOrdersValue))
                                    .withBalance(entry.getValue().balance.add(sellOrdersValue));
                            entry.setValue(newValue);
                        }
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            orders = this.orders;
            currencies = this.currencies;
            positions = this.positions;
        }

        return new TradingState(
                this.orderbook,
                this.candle,
                instrumentInfo,
                orders,
                currencies,
                positions,
                this.instrumentCurrency,
                this.operations,
                this.waitingForPlacingOrder
        );
    }

    @NotNull
    public TradingState withExecutedOrder(@NotNull final String id) {
        Objects.requireNonNull(instrumentInfo);

        Order executedOrder = null;
        for (final Order o : this.orders) {
            if (o.id.equals(id)) {
                executedOrder = o;
            }
        }

        final List<Order> filtered = this.orders.stream().filter(o -> !o.id.equals(id)).collect(Collectors.toList());

        final Map<Currency, Position> currenciesCopy = new HashMap<>(this.currencies);
        final Map<String, Position> positionsCopy = new HashMap<>(this.positions);
        if (Objects.nonNull(executedOrder)) {
            final BigDecimal executedLots = BigDecimal.valueOf(executedOrder.executedLots).multiply(BigDecimal.valueOf(instrumentInfo.lot));
            final BigDecimal amount = executedOrder.price.multiply(executedLots);
            final Position currentCurrencyPosition = currenciesCopy.getOrDefault(this.instrumentCurrency, Position.empty);
            final Position newCurrencyValue = executedOrder.operation == Order.Type.Buy
                    ? currentCurrencyPosition.withBlocked(currentCurrencyPosition.blocked.subtract(amount))
                    : currentCurrencyPosition.withBalance(currentCurrencyPosition.balance.add(amount));
            currenciesCopy.put(this.instrumentCurrency, newCurrencyValue);

            final Position currentPosition = positionsCopy.getOrDefault(executedOrder.figi, Position.empty);
            final Position newPositionValue = executedOrder.operation == Order.Type.Buy
                    ? currentPosition.withBalance(currentPosition.balance.add(executedLots))
                    : currentPosition.withBlocked(currentPosition.blocked.subtract(executedLots));
            positionsCopy.put(executedOrder.figi, newPositionValue);
        }

        Operation oldOperation = null;
        for (final Operation o : this.operations) {
            if (o.id.equals(id)) {
                oldOperation = o;
            }
        }

        final List<Operation> filteredOps = this.operations.stream().filter(o -> o.id.equals(id)).collect(Collectors.toList());
        if (Objects.nonNull(oldOperation)) {
            final Operation newOperation = oldOperation.execute();
            filteredOps.add(newOperation);
        }

        return new TradingState(
                this.orderbook,
                this.candle,
                this.instrumentInfo,
                filtered,
                currenciesCopy,
                positionsCopy,
                this.instrumentCurrency,
                this.operations,
                this.waitingForPlacingOrder
        );
    }

    @NotNull
    public TradingState withCancelledOrder(@NotNull final String id) {
        Objects.requireNonNull(instrumentInfo);

        Order cancelledOrder = null;
        for (final Order o : this.orders) {
            if (o.id.equals(id)) {
                cancelledOrder = o;
            }
        }

        final List<Order> filtered = this.orders.stream().filter(o -> o.id.equals(id)).collect(Collectors.toList());

        final Map<Currency, Position> currenciesCopy = new HashMap<>(this.currencies);
        final Map<String, Position> positionsCopy = new HashMap<>(this.positions);
        if (Objects.nonNull(cancelledOrder)) {
            final BigDecimal requestedLots = BigDecimal.valueOf(cancelledOrder.requestedLots).multiply(BigDecimal.valueOf(instrumentInfo.lot));
            final BigDecimal amount = cancelledOrder.price.multiply(requestedLots);
            final Position currentCurrencyPosition = currenciesCopy.getOrDefault(this.instrumentCurrency, Position.empty);
            final Position newCurrencyValue = cancelledOrder.operation == Order.Type.Buy
                    ? currentCurrencyPosition.withBlocked(currentCurrencyPosition.blocked.subtract(amount)).withBalance(currentCurrencyPosition.balance.add(amount))
                    : currentCurrencyPosition;
            currenciesCopy.put(this.instrumentCurrency, newCurrencyValue);

            final Position currentPosition = positionsCopy.getOrDefault(cancelledOrder.figi, Position.empty);
            final Position newPositionValue = cancelledOrder.operation == Order.Type.Buy
                    ? currentPosition
                    : currentPosition.withBlocked(currentPosition.blocked.subtract(requestedLots)).withBalance(currentPosition.balance.add(requestedLots));
            positionsCopy.put(cancelledOrder.figi, newPositionValue);
        }

        Operation oldOperation = null;
        for (final Operation o : this.operations) {
            if (o.id.equals(id)) {
                oldOperation = o;
            }
        }

        final List<Operation> filteredOps = this.operations.stream().filter(o -> o.id.equals(id)).collect(Collectors.toList());
        if (Objects.nonNull(oldOperation)) {
            final Operation newOperation = oldOperation.cancel();
            filteredOps.add(newOperation);
        }

        return new TradingState(
                this.orderbook,
                this.candle,
                this.instrumentInfo,
                filtered,
                currenciesCopy,
                positionsCopy,
                this.instrumentCurrency,
                filteredOps,
                this.waitingForPlacingOrder
        );
    }

    @NotNull
    public TradingState withChangedOrder(@NotNull final String id,
                                         @NotNull final Order.Status status,
                                         final int executedLots) {
        final List<Order> updated = this.orders.stream().map(o -> {
            if (o.id.equals(id)) {
                return new Order(
                        o.id,
                        o.operation,
                        status,
                        o.requestedLots,
                        executedLots,
                        o.price,
                        o.figi
                );
            } else {
                return o;
            }
        }).collect(Collectors.toList());

        Operation oldOperation = null;
        for (final Operation o : this.operations) {
            if (o.id.equals(id)) {
                oldOperation = o;
            }
        }

        final List<Operation> filteredOps = this.operations.stream().filter(o -> o.id.equals(id)).collect(Collectors.toList());
        if (Objects.nonNull(oldOperation)) {
            final Operation newOperation = oldOperation.cancel();
            filteredOps.add(newOperation);
        }

        return new TradingState(
                this.orderbook,
                this.candle,
                this.instrumentInfo,
                updated,
                this.currencies,
                this.positions,
                this.instrumentCurrency,
                filteredOps,
                this.waitingForPlacingOrder
        );
    }

    @NotNull
    public TradingState withPlacedOrder(@NotNull final Order newOrder) {
        Objects.requireNonNull(instrumentInfo);

        final List<Order> newOrders = new LinkedList<>(this.orders);
        newOrders.add(newOrder);

        final Map<Currency, Position> currenciesCopy = new HashMap<>(this.currencies);
        final Map<String, Position> positionsCopy = new HashMap<>(this.positions);

        final BigDecimal requestedLots = BigDecimal.valueOf(newOrder.requestedLots).multiply(BigDecimal.valueOf(instrumentInfo.lot));
        final BigDecimal amount = newOrder.price.multiply(requestedLots);
        final Position currentCurrencyPosition = currenciesCopy.getOrDefault(this.instrumentCurrency, Position.empty);
        final Position newCurrencyValue = newOrder.operation == Order.Type.Buy
                ? currentCurrencyPosition.withBlocked(currentCurrencyPosition.blocked.add(amount)).withBalance(currentCurrencyPosition.balance.subtract(amount))
                : currentCurrencyPosition;
        currenciesCopy.put(this.instrumentCurrency, newCurrencyValue);

        final Position currentPosition = positionsCopy.getOrDefault(newOrder.figi, Position.empty);
        final Position newPositionValue = newOrder.operation == Order.Type.Buy
                ? currentPosition
                : currentPosition.withBlocked(currentPosition.blocked.add(requestedLots)).withBalance(currentPosition.balance.subtract(requestedLots));
        positionsCopy.put(newOrder.figi, newPositionValue);

        final Operation newOperation = Operation.fromOrder(newOrder, instrumentCurrency);
        final List<Operation> newOperations = new LinkedList<>(this.operations);
        newOperations.add(newOperation);

        return new TradingState(
                this.orderbook,
                this.candle,
                this.instrumentInfo,
                newOrders,
                currenciesCopy,
                positionsCopy,
                this.instrumentCurrency,
                newOperations,
                false
        );
    }

    @NotNull
    public TradingState withUnplacedOrder() {
        return new TradingState(
                this.orderbook,
                this.candle,
                this.instrumentInfo,
                this.orders,
                this.currencies,
                this.positions,
                this.instrumentCurrency,
                this.operations,
                false
        );
    }

    @Override
    public String toString() {
        return "TradingState{" +
                "orderbook=" + orderbook +
                ", candle=" + candle +
                ", instrumentInfo=" + instrumentInfo +
                ", orders=" + orders +
                ", currencies=" + currencies +
                ", positions=" + positions +
                ", instrumentCurrency=" + instrumentCurrency +
                ", waitingForPlacingOrder=" + waitingForPlacingOrder +
                '}';
    }

    public static class Candle {

        @NotNull
        public final BigDecimal openPrice;
        @NotNull
        public final BigDecimal closingPrice;
        @NotNull
        public final BigDecimal highestPrice;
        @NotNull
        public final BigDecimal lowestPrice;
        @NotNull
        public final BigDecimal tradingValue;
        @NotNull
        public final ZonedDateTime dateTime;
        @NotNull
        public final CandleInterval interval;
        @NotNull
        public final String figi;

        public Candle(@NotNull final BigDecimal openPrice,
                      @NotNull final BigDecimal closingPrice,
                      @NotNull final BigDecimal highestPrice,
                      @NotNull final BigDecimal lowestPrice,
                      @NotNull final BigDecimal tradingValue,
                      @NotNull final ZonedDateTime dateTime,
                      @NotNull final CandleInterval interval,
                      @NotNull final String figi) {
            this.openPrice = openPrice;
            this.closingPrice = closingPrice;
            this.highestPrice = highestPrice;
            this.lowestPrice = lowestPrice;
            this.tradingValue = tradingValue;
            this.dateTime = dateTime;
            this.interval = interval;
            this.figi = figi;
        }

        public enum CandleInterval {
            ONE_MIN, TWO_MIN, THREE_MIN, FIVE_MIN, TEN_MIN, QUARTER_HOUR, HALF_HOUR, HOUR, DAY, WEEK, MONTH
        }

        @Override
        public String toString() {
            return "Candle{" +
                    "openPrice=" + openPrice +
                    ", closingPrice=" + closingPrice +
                    ", highestPrice=" + highestPrice +
                    ", lowestPrice=" + lowestPrice +
                    ", tradingValue=" + tradingValue +
                    ", dateTime=" + dateTime +
                    ", interval=" + interval +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static class Orderbook {

        public final int depth;
        @NotNull
        public final List<StakeState> bids;
        @NotNull
        public final List<StakeState> asks;
        @NotNull
        public final String figi;

        public Orderbook(final int depth,
                         @NotNull final List<StakeState> bids,
                         @NotNull final List<StakeState> asks,
                         @NotNull final String figi) {
            this.depth = depth;
            this.bids = bids;
            this.asks = asks;
            this.figi = figi;
        }

        public static class StakeState {
            @NotNull
            public final BigDecimal price;
            public final int count;

            public StakeState(@NotNull final BigDecimal price, final int count) {
                this.price = price;
                this.count = count;
            }

            @Override
            public String toString() {
                return "StakeState{" +
                        "price=" + price +
                        ", count=" + count +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Orderbook{" +
                    "depth=" + depth +
                    ", bids=" + bids +
                    ", asks=" + asks +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static class InstrumentInfo {
        public final boolean canTrade;
        @NotNull
        public final BigDecimal minPriceIncrement;
        public final int lot;
        @Nullable
        public final BigDecimal accruedInterest;
        @Nullable
        public final BigDecimal limitUp;
        @Nullable
        public final BigDecimal limitDown;
        @NotNull
        public final String figi;

        public InstrumentInfo(final boolean canTrade,
                              @NotNull final BigDecimal minPriceIncrement,
                              final int lot,
                              @Nullable final BigDecimal accruedInterest,
                              @Nullable final BigDecimal limitUp,
                              @Nullable final BigDecimal limitDown,
                              @NotNull final String figi) {
            this.canTrade = canTrade;
            this.minPriceIncrement = minPriceIncrement;
            this.lot = lot;
            this.accruedInterest = accruedInterest;
            this.limitUp = limitUp;
            this.limitDown = limitDown;
            this.figi = figi;
        }

        @Override
        public String toString() {
            return "InstrumentInfo{" +
                    "canTrade=" + canTrade +
                    ", minPriceIncrement=" + minPriceIncrement +
                    ", lot=" + lot +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static class Order {
        @NotNull
        public final String id;
        @NotNull
        public final Type operation;
        @NotNull
        public final Status status;
        public final int requestedLots;
        public final int executedLots;
        @NotNull
        public final BigDecimal price;
        @NotNull
        public final String figi;

        public Order(@NotNull final String id,
                     @NotNull final Type operation,
                     @NotNull final Status status,
                     final int requestedLots,
                     final int executedLots,
                     @NotNull final BigDecimal price,
                     @NotNull final String figi) {

            this.id = id;
            this.operation = operation;
            this.status = status;
            this.requestedLots = requestedLots;
            this.executedLots = executedLots;
            this.price = price;
            this.figi = figi;
        }

        public enum Status {
            New, PartiallyFill, Fill, Cancelled, Replaced, PendingCancel, Rejected, PendingReplace, PendingNew
        }

        public enum Type {
            Buy, Sell
        }

        @Override
        public String toString() {
            return "Order{" +
                    "id='" + id + '\'' +
                    ", operation=" + operation +
                    ", status=" + status +
                    //", rejectReason='" + rejectReason + '\'' +
                    ", requestedLots=" + requestedLots +
                    ", executedLots=" + executedLots +
                    //", commission=" + commission +
                    ", price=" + price +
                    ", figi='" + figi + '\'' +
                    '}';
        }
    }

    public static class MoneyAmount {
        @NotNull
        public final Currency currency;
        @NotNull
        public final BigDecimal value;

        public MoneyAmount(@NotNull final Currency currency,
                           @NotNull final BigDecimal value) {
            this.currency = currency;
            this.value = value;
        }

        @Override
        public String toString() {
            return "MoneyAmount{" +
                    "currency=" + currency +
                    ", value=" + value +
                    '}';
        }
    }

    public enum Currency {
        RUB, USD, EUR
    }

    public static class Position {
        @NotNull
        public final BigDecimal balance;
        @NotNull
        public final BigDecimal blocked;

        public static Position empty = new Position(BigDecimal.ZERO, BigDecimal.ZERO);

        public Position(@NotNull final BigDecimal balance,
                        @Nullable final BigDecimal blocked) {
            this.balance = balance;
            this.blocked = Objects.nonNull(blocked) ? blocked : BigDecimal.ZERO;
        }

        @NotNull
        public Position withBalance(@NotNull final BigDecimal newBalance) {
            return new Position(newBalance, this.blocked);
        }

        @NotNull
        public Position withBlocked(@NotNull final BigDecimal newBlocked) {
            return new Position(this.balance, newBlocked);
        }

        @Override
        public String toString() {
            return "Position{" +
                    "balance=" + balance +
                    ", blocked=" + blocked +
                    '}';
        }
    }

    public static class Operation {
        @NotNull
        public final String id;
        @NotNull
        public final Status status;
        @Nullable
        private final List<Trade> trades;
        @Nullable
        private final MoneyAmount commission;
        @NotNull
        public final Currency currency;
        @NotNull
        public final BigDecimal payment;
        @Nullable
        private final BigDecimal price;
        private final int quantity;
        @Nullable
        private final String figi;
        @Nullable
        private final InstrumentType instrumentType;
        @NotNull
        public final Boolean isMarginCall;
        @NotNull
        public final OffsetDateTime date;
        @Nullable
        private final Type operationType;

        public static Operation fromOrder(@NotNull final Order newOrder, @NotNull final Currency currency) {
            return new Operation(newOrder.id,
                    Status.Progress,
                    null,
                    null,
                    currency,
                    newOrder.price.multiply(BigDecimal.valueOf(newOrder.requestedLots)),
                    newOrder.price,
                    newOrder.requestedLots,
                    newOrder.figi,
                    null,
                    false,
                    OffsetDateTime.now(),
                    newOrder.operation == Order.Type.Buy ? Type.Buy : Type.Sell);
        }

        private Operation(@NotNull final String id,
                          @NotNull final Status status,
                          @Nullable final List<Trade> trades,
                          @Nullable final MoneyAmount commission,
                          @NotNull final Currency currency,
                          @NotNull final BigDecimal payment,
                          @Nullable final BigDecimal price,
                          @Nullable final Integer quantity,
                          @Nullable final String figi,
                          @Nullable final InstrumentType instrumentType,
                          @NotNull final Boolean isMarginCall,
                          @NotNull final OffsetDateTime date,
                          @Nullable final Type operationType) {
            this.id = id;
            this.status = status;
            this.trades = trades;
            this.commission = commission;
            this.currency = currency;
            this.payment = operationType == Type.Buy ? payment.negate() : payment;
            this.price = price;
            this.quantity = Objects.nonNull(quantity) ? quantity : -1;
            this.figi = figi;
            this.instrumentType = instrumentType;
            this.isMarginCall = isMarginCall;
            this.date = date;
            this.operationType = operationType;
        }

        @NotNull
        public Optional<List<Trade>> trades() {
            return Optional.ofNullable(trades);
        }

        @NotNull
        public OptionalInt quantity() {
            return quantity < 0 ? OptionalInt.empty() : OptionalInt.of(quantity);
        }

        @NotNull
        public Optional<MoneyAmount> commission() {
            return Optional.ofNullable(commission);
        }

        @NotNull
        public Optional<BigDecimal> price() {
            return Optional.ofNullable(price);
        }

        @NotNull
        public Optional<String> figi() {
            return Optional.ofNullable(figi);
        }

        @NotNull
        public Optional<InstrumentType> instrumentType() {
            return Optional.ofNullable(instrumentType);
        }

        @NotNull
        public Optional<Type> getOperationType() {
            return Optional.ofNullable(operationType);
        }

        @NotNull
        public Operation cancel() {
            return new Operation(
                    this.id,
                    Status.Decline,
                    this.trades,
                    this.commission,
                    this.currency,
                    this.payment,
                    this.price,
                    this.quantity,
                    this.figi,
                    this.instrumentType,
                    this.isMarginCall,
                    OffsetDateTime.now(),
                    this.operationType
            );
        }

        @NotNull
        public Operation execute() {
            return new Operation(
                    this.id,
                    Status.Done,
                    this.trades,
                    this.commission,
                    this.currency,
                    this.payment,
                    this.price,
                    this.quantity,
                    this.figi,
                    this.instrumentType,
                    this.isMarginCall,
                    OffsetDateTime.now(),
                    this.operationType
            );
        }

        @NotNull
        public Operation update(final int executedLots) {
            final List<Trade> newTrades = Objects.isNull(this.trades) ? new LinkedList<>() : new LinkedList<>(this.trades);
            final int currentTradesCount = newTrades.size();
            final int moreTradesCount = executedLots - currentTradesCount;
            final OffsetDateTime now = OffsetDateTime.now();
            newTrades.add(new Trade("fakeId", now, BigDecimal.ZERO, moreTradesCount));

            return new Operation(
                    this.id,
                    Status.Progress,
                    newTrades,
                    this.commission,
                    this.currency,
                    this.payment,
                    this.price,
                    this.quantity,
                    this.figi,
                    this.instrumentType,
                    this.isMarginCall,
                    now,
                    this.operationType
            );
        }

        public enum Status {
            Done, Decline, Progress
        }

        public enum Type {
            Buy, Sell, BrokerCommission, ExchangeCommission, ServiceCommission, MarginCommission, BuyCard, Dividend, TaxDividend, TaxCoupon, Coupon, PartRepayment, PayIn
        }

        public static class Trade {
            @NotNull
            public final String tradeId;
            @NotNull
            public final OffsetDateTime date;
            @NotNull
            public final BigDecimal price;
            public final int quantity;

            public Trade(@NotNull final String tradeId,
                         @NotNull final OffsetDateTime date,
                         @NotNull final BigDecimal price,
                         final int quantity) {
                this.tradeId = tradeId;
                this.date = date;
                this.price = price;
                this.quantity = quantity;
            }
        }
    }

    public enum InstrumentType {
        Stock, Currency, Bond, ETF
    }

}
