package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Logger;

public class SimpleStopLossStrategy implements Strategy {

    private enum LastOrderResult { Profit, Loss, None }
    private enum CurrentPositionStatus { Exists, Processing, None }

    private final List<Order> placedOrders;
    private final SubmissionPublisher<LimitOrder> placeLimitOrderPublisher;
    private final SubmissionPublisher<String> cancelLimitOrderPublisher;
    private final Instrument operatingInstrument;
    private final BigDecimal maxOperationValue;
    private final int maxOperationOrderbookDepth;
    private final CandleInterval candlesOperationInterval;
    private final BigDecimal fallToGrowInterest;
    private final BigDecimal growToFallInterest;
    private final BigDecimal profitInterest;
    private final BigDecimal stopLossInterest;
    private final Logger logger;

    private PortfolioCurrencies.PortfolioCurrency currencyPosition;
    private volatile CurrentPositionStatus currentPositionStatus;
    private LastOrderResult lastOrderResult;
    private BigDecimal extremum;
    private BigDecimal initialPrice;
    private boolean canTrade;

    public SimpleStopLossStrategy(PortfolioCurrencies.PortfolioCurrency currencyPosition,
                                  List<Order> placedOrders,
                                  Instrument operatingInstrument,
                                  BigDecimal maxOperationValue,
                                  int maxOperationOrderbookDepth,
                                  CandleInterval candlesOperationInterval,
                                  BigDecimal growToFallInterest,
                                  BigDecimal fallToGrowInterest,
                                  BigDecimal profitInterest,
                                  BigDecimal stopLossInterest,
                                  Logger logger) {

        if (!(maxOperationValue.compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("maxOperationValue должно быть положительным");
        }
        if (maxOperationOrderbookDepth <= 0) {
            throw new IllegalArgumentException("maxOperationOrderbookDepth должно быть положительным");
        }
        if (!(growToFallInterest.compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("growToFallInterest должно быть положительным");
        }
        if (!(fallToGrowInterest.compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("fallToGrowInterest должно быть положительным");
        }
        if (!(profitInterest.compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("profitInterest должно быть положительным");
        }
        if (!(stopLossInterest.compareTo(BigDecimal.ZERO) > 0)) {
            throw new IllegalArgumentException("stopLossInterest должно быть положительным");
        }

        this.placedOrders = placedOrders;
        this.currencyPosition = currencyPosition;
        this.operatingInstrument = operatingInstrument;
        this.maxOperationValue = maxOperationValue;
        this.maxOperationOrderbookDepth = maxOperationOrderbookDepth;
        this.candlesOperationInterval = candlesOperationInterval;
        this.fallToGrowInterest = fallToGrowInterest;
        this.growToFallInterest = growToFallInterest;
        this.profitInterest = profitInterest;
        this.stopLossInterest = stopLossInterest;
        this.logger = logger;

        this.placeLimitOrderPublisher = new SubmissionPublisher<>();
        this.cancelLimitOrderPublisher = new SubmissionPublisher<>();

        this.currentPositionStatus = CurrentPositionStatus.None;
        this.lastOrderResult = LastOrderResult.None;
        this.canTrade = false;
    }

    @Override
    public Instrument getInstrument() {
        return this.operatingInstrument;
    }

    @Override
    public CandleInterval getCandleInterval() {
        return this.candlesOperationInterval;
    }

    @Override
    public int getOrderbookDepth() {
        return this.maxOperationOrderbookDepth;
    }

    @Override
    public void consumeCandle(StreamingEvent.Candle candle) {
        final var price = candle.getHighestPrice().add(candle.getLowestPrice())
                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_EVEN);

        if (currentPositionStatus == CurrentPositionStatus.None && lastOrderResult == LastOrderResult.None) {
            if (!canTrade) {
                logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                        ". Экстремум = " + extremum + ". Сейчас нет позиции и до этого не было. Нельзя торговать. " +
                        "Ничего не делаем.");
                return;
            }

            logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                    ". Экстремум = " + extremum + ". Сейчас нет позиции и до этого не было. Можно торговать. " +
                    "Размещаем лимитную заявку на покупку.");
            placeLimitOrder(price, OperationType.Buy);
        } else if (currentPositionStatus == CurrentPositionStatus.None) {
            if (price.compareTo(extremum) <= 0) {
                logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                        ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого была. Текущая цена <= экстремуму. " +
                        "Обновляем экстремум ценой.");
                extremum = price;
            } else {
                final var delta = price.subtract(extremum); // always positive
                final var percent = delta.divide(
                        extremum.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                        RoundingMode.HALF_EVEN);
                if (percent.compareTo(fallToGrowInterest) > 0) {
                    if (canTrade) {
                        logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого была. Текущая цена > экстремума. " +
                                "Цена поднялась значительно относительно экстремума. Можно торговать. Размещаем лимитную заявку на покупку.");
                        placeLimitOrder(price, OperationType.Buy);
                    } else {
                        logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого была. Текущая цена > экстремума. " +
                                "Цена поднялась значительно относительно экстремума. Нельзя торговать. Ничего не делаем.");
                    }
                } else {
                    logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                            ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого была. Текущая цена > экстремума. " +
                            "Цена поднялась незначительно относительно экстремума. Ничего не делаем.");
                }
            }
        } else if (currentPositionStatus == CurrentPositionStatus.Exists) {
            if (extremum.compareTo(initialPrice) > 0) {
                if (price.compareTo(extremum) >= 0) {
                    logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                            ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > отсчётной цены. " +
                            "Текущая цена >= экстремуму. Обновляем экстремум ценой.");
                    extremum = price;
                } else {
                    final var extrAndInitDelta = extremum.subtract(initialPrice);
                    final var extrAndInitPercent = extrAndInitDelta.divide(
                            initialPrice.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                            RoundingMode.HALF_EVEN);
                    if (extrAndInitPercent.compareTo(profitInterest) >= 0) {
                        final var extrAndPriceDelta = extremum.subtract(price); // always positive
                        final var extrAndPricePercent = extrAndPriceDelta.divide(
                                extremum.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                                RoundingMode.HALF_EVEN);
                        if (extrAndPricePercent.compareTo(growToFallInterest) >= 0) {
                            logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                    ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > отсчётной цены. " +
                                    "Текущая цена < экстремума. Экстремум поднялся значительно относительно отсчётной цены. " +
                                    "Цена опустилась значительно ниже экстремума. Размещаем лимитную заявку на продажу (фиксация прибыли).");
                            lastOrderResult = LastOrderResult.Profit;
                            placeLimitOrder(price, OperationType.Sell);
                        } else {
                            logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                    ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > отсчётной цены. " +
                                    "Текущая цена < экстремума. Цена опустилась незначительно ниже экстремума. Ничего не делаем.");
                        }
                    } else {
                        logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > отсчётной цены. " +
                                "Текущая цена < экстремума. Экстремум поднялся незначительно относительно отсчётной цены. Обновляем экстремум ценой.");
                        extremum = price;
                    }
                }
            } else if (extremum.compareTo(initialPrice) < 0) {
                if (price.compareTo(extremum) >= 0) {
                    logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                            ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум < отсчётной цены. " +
                            "Текущая цена >= экстремуму. Обновляем экстремум ценой.");
                    extremum = price;
                } else {
                    final var priceAndInitDelta = price.subtract(initialPrice).abs();
                    final var priceAndInitPercent = priceAndInitDelta.divide(
                            initialPrice.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                            RoundingMode.HALF_EVEN);
                    if (priceAndInitPercent.compareTo(stopLossInterest) >= 0) {
                        logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум < отсчётной цены. " +
                                "Текущая цена < экстремума. Цена опустилась значительно относительно отсчётной цены. " +
                                "Размещаем лимитную заявку на продажу (остановка потерь).");
                        lastOrderResult = LastOrderResult.Loss;
                        placeLimitOrder(price, OperationType.Sell);
                    } else {
                        logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                                ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум < отсчётной цены. " +
                                "Текущая цена < экстремума. Цена опустилась незначительно относительно отсчётной цены. " +
                                "Обновляем экстремум ценой.");
                        extremum = price;
                    }
                }
            } else {
                logger.fine("Новая свечка получена. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                        ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум = отсчётной цене. " +
                        "Обновляем экстремум ценой.");
                extremum = price;
            }
        }
    }

    @Override
    public void consumeOrderbook(StreamingEvent.Orderbook orderbook) {
    }

    @Override
    public void consumeInstrumentInfo(StreamingEvent.InstrumentInfo instrumentInfo) {
        final var newCanTrade = instrumentInfo.canTrade();
        if (newCanTrade != this.canTrade)
            logger.fine("Изменился торговый статус инструмента: " + instrumentInfo.getTradeStatus());
        this.canTrade = newCanTrade;
    }

    @Override
    public Flow.Publisher<LimitOrder> getLimitOrderPublisher() {
        return this.placeLimitOrderPublisher;
    }

    @Override
    public Flow.Publisher<String> getCancelPublisher() {
        return this.cancelLimitOrderPublisher;
    }

    @Override
    public void consumePlacedLimitOrder(PlacedLimitOrder placedLimitOrder) {
        currentPositionStatus = placedLimitOrder.getOperation() == OperationType.Buy
                ? CurrentPositionStatus.Exists
                : CurrentPositionStatus.None;
    }

    @Override
    public void consumeRejectedLimitOrder(OperationType operationType) {
        currentPositionStatus = operationType == OperationType.Buy
                ? CurrentPositionStatus.None
                : CurrentPositionStatus.Exists;
    }

    @Override
    public void init() {
        placedOrders.stream()
                .filter(placedOrder -> placedOrder.getFigi().equals(operatingInstrument.getFigi()))
                .forEach(placedOrder -> cancelLimitOrderPublisher.submit(placedOrder.getId()));
    }

    private void placeLimitOrder(BigDecimal price, OperationType operationType) {
        currentPositionStatus = CurrentPositionStatus.Processing;
        initialPrice = price;
        extremum = price;
        currencyPosition = new PortfolioCurrencies.PortfolioCurrency(
                currencyPosition.getCurrency(),
                currencyPosition.getBalance().subtract(price),
                currencyPosition.getBlocked()
        );

        final var maxValue = maxOperationValue.compareTo(currencyPosition.getBalance()) > 0
                ? currencyPosition.getBalance()
                : maxOperationValue;
        final var lots = maxValue.divide(
                price.multiply(BigDecimal.valueOf(operatingInstrument.getLot())), RoundingMode.DOWN);
        final var firstLimitOrder = new LimitOrder(
                operatingInstrument.getFigi(),
                lots.intValue(),
                operationType,
                price
        );
        placeLimitOrderPublisher.submit(firstLimitOrder);
    }
}
