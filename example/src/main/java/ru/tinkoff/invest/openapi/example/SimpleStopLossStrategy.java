package ru.tinkoff.invest.openapi.example;

import ru.tinkoff.invest.openapi.automata.Instrument;
import ru.tinkoff.invest.openapi.automata.Strategy;
import ru.tinkoff.invest.openapi.automata.StrategyDecision;
import ru.tinkoff.invest.openapi.automata.TradingState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

public class SimpleStopLossStrategy implements Strategy {

    private enum LastOrderResult {Profit, Loss, None}

    private final Instrument operatingInstrument;
    private final BigDecimal maxOperationValue;
    private final int maxOperationOrderbookDepth;
    private final TradingState.Candle.CandleInterval candlesOperationInterval;
    private final BigDecimal fallToGrowInterest;
    private final BigDecimal growToFallInterest;
    private final BigDecimal profitInterest;
    private final BigDecimal stopLossInterest;
    private final Logger logger;

    private LastOrderResult lastOrderResult;
    private BigDecimal extremum;
    private BigDecimal initialPrice;
    private boolean canTrade;
    private TradingState currentState;

    public SimpleStopLossStrategy(final Instrument operatingInstrument,
                                  final List<TradingState.Order> orders,
                                  final Map<TradingState.Currency, TradingState.Position> currencies,
                                  final Map<String, TradingState.Position> positions,
                                  final BigDecimal maxOperationValue,
                                  final int maxOperationOrderbookDepth,
                                  final TradingState.Candle.CandleInterval candlesOperationInterval,
                                  final BigDecimal growToFallInterest,
                                  final BigDecimal fallToGrowInterest,
                                  final BigDecimal profitInterest,
                                  final BigDecimal stopLossInterest,
                                  final Logger logger) {

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

        this.operatingInstrument = operatingInstrument;
        this.maxOperationValue = maxOperationValue;
        this.maxOperationOrderbookDepth = maxOperationOrderbookDepth;
        this.candlesOperationInterval = candlesOperationInterval;
        this.fallToGrowInterest = fallToGrowInterest;
        this.growToFallInterest = growToFallInterest;
        this.profitInterest = profitInterest;
        this.stopLossInterest = stopLossInterest;
        this.logger = logger;

        this.lastOrderResult = LastOrderResult.None;
        this.canTrade = false;

        currentState = TradingState.init(
                orders,
                currencies,
                positions,
                operatingInstrument.currency
        );
    }

    @Override
    public TradingState.Candle.CandleInterval getCandleInterval() {
        return this.candlesOperationInterval;
    }

    @Override
    public int getOrderbookDepth() {
        return this.maxOperationOrderbookDepth;
    }

    @Override
    public TradingState getCurrentState() {
        return this.currentState;
    }

    public StrategyDecision handleNewState(final TradingState tradingState) {
        currentState = tradingState;
        final var candle = currentState.candle;
        final var instrumentInfo = currentState.instrumentInfo;

        checkCanTrade(instrumentInfo);

        if (candle == null) {
            return StrategyDecision.pass();
        }

        final var price = candle.highestPrice.add(candle.lowestPrice)
                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_EVEN);

        final TradingState.Position position = currentState.positions.getOrDefault(operatingInstrument.figi, TradingState.Position.empty);
        final boolean hasPosition = position.balance.add(position.blocked).compareTo(BigDecimal.ZERO) != 0;

        if (currentState.waitingForPlacingOrder) {
            logger.finest("Состояние поменялось. Идёт выставление заявки. Ничего не делаем.");
            return StrategyDecision.pass();
        } else if (!currentState.orders.isEmpty()) {
            logger.finest("Состояние поменялось. Сейчас есть заявка. Ничего не делаем.");
            return StrategyDecision.pass();
        } else if (hasPosition && price.compareTo(extremum) == 0) {
            logger.finest("Состояние поменялось. Сейчас есть позиция. Цена не изменилась. Ничего не делаем.");
            return StrategyDecision.pass();
        } else if (!hasPosition && lastOrderResult == LastOrderResult.None) {
            if (!canTrade) {
                logger.fine("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                        initialPrice + ". Экстремум = " + extremum + ". Сейчас нет позиции и до этого не было. " +
                        "Нельзя торговать. Ничего не делаем.");
                return StrategyDecision.pass();
            }

            logger.fine("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " + initialPrice +
                    ". Экстремум = " + extremum + ". Сейчас нет позиции и до этого не было. Можно торговать. " +
                    "Размещаем лимитную заявку на покупку.");
            return placeLimitOrder(price, TradingState.Order.Type.Buy);
        } else if (!hasPosition && lastOrderResult != LastOrderResult.None) {
            if (price.compareTo(extremum) <= 0) {
                logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                        initialPrice + ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого была. " +
                        "Текущая цена <= экстремуму. Обновляем экстремум ценой.");
                extremum = price;
            } else {
                final var delta = price.subtract(extremum); // результат всегда положительный
                final var percent = delta.divide(
                        extremum.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                        RoundingMode.HALF_EVEN);
                if (percent.compareTo(fallToGrowInterest) > 0) {
                    if (canTrade) {
                        logger.fine("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                initialPrice + ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого " +
                                "была. Текущая цена > экстремума. Цена поднялась значительно относительно " +
                                "экстремума. Можно торговать. Размещаем лимитную заявку на покупку.");
                        return placeLimitOrder(price, TradingState.Order.Type.Buy);
                    } else {
                        logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                initialPrice + ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого " +
                                "была. Текущая цена > экстремума. Цена поднялась значительно относительно " +
                                "экстремума. Нельзя торговать. Ничего не делаем.");
                    }
                } else {
                    logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                            initialPrice + ". Экстремум = " + extremum + ". Сейчас нет позиции, но до этого была. " +
                            "Текущая цена > экстремума. Цена поднялась незначительно относительно экстремума. " +
                            "Ничего не делаем.");
                }
            }
        } else if (hasPosition) {
            if (extremum.compareTo(initialPrice) > 0) {
                if (price.compareTo(extremum) >= 0) {
                    logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                            initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > " +
                            "отсчётной цены. Текущая цена >= экстремуму. Обновляем экстремум ценой.");
                    extremum = price;
                } else {
                    final var extrAndInitDelta = extremum.subtract(initialPrice);
                    final var extrAndInitPercent = extrAndInitDelta.divide(
                            initialPrice.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                            RoundingMode.HALF_EVEN);
                    if (extrAndInitPercent.compareTo(profitInterest) >= 0) {
                        final var extrAndPriceDelta = extremum.subtract(price); // результат всегда положительный
                        final var extrAndPricePercent = extrAndPriceDelta.divide(
                                extremum.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                                RoundingMode.HALF_EVEN);
                        if (extrAndPricePercent.compareTo(growToFallInterest) >= 0) {
                            logger.fine("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                    initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > " +
                                    "отсчётной цены. Текущая цена < экстремума. Экстремум поднялся значительно " +
                                    "относительно отсчётной цены. Цена опустилась значительно ниже экстремума. " +
                                    "Размещаем лимитную заявку на продажу (фиксация прибыли).");
                            lastOrderResult = LastOrderResult.Profit;
                            return placeLimitOrder(price, TradingState.Order.Type.Sell);
                        } else {
                            logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                    initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > " +
                                    "отсчётной цены. Текущая цена < экстремума. Цена опустилась незначительно ниже " +
                                    "экстремума. Ничего не делаем.");
                        }
                    } else {
                        logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум > " +
                                "отсчётной цены. Текущая цена < экстремума. Экстремум поднялся незначительно " +
                                "относительно отсчётной цены. Обновляем экстремум ценой.");
                        extremum = price;
                    }
                }
            } else if (extremum.compareTo(initialPrice) < 0) {
                if (price.compareTo(extremum) >= 0) {
                    logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                            initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум < " +
                            "отсчётной цены. Текущая цена >= экстремуму. Обновляем экстремум ценой.");
                    extremum = price;
                } else {
                    final var priceAndInitDelta = price.subtract(initialPrice).abs();
                    final var priceAndInitPercent = priceAndInitDelta.divide(
                            initialPrice.divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN),
                            RoundingMode.HALF_EVEN);
                    if (priceAndInitPercent.compareTo(stopLossInterest) >= 0) {
                        logger.fine("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум < " +
                                "отсчётной цены. екущая цена < экстремума. Цена опустилась значительно относительно " +
                                "отсчётной цены. Размещаем лимитную заявку на продажу (остановка потерь).");
                        lastOrderResult = LastOrderResult.Loss;
                        return placeLimitOrder(price, TradingState.Order.Type.Sell);
                    } else {
                        logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                                initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум < " +
                                "отсчётной цены. Текущая цена < экстремума. Цена опустилась незначительно " +
                                "относительно отсчётной цены. Обновляем экстремум ценой.");
                        extremum = price;
                    }
                }
            } else {
                logger.finest("Состояние поменялось. Текущая цена = " + price + ". Отсчётная цена = " +
                        initialPrice + ". Экстремум = " + extremum + ". Сейчас есть позиция. Экстремум = отсчётной " +
                        "цене. Обновляем экстремум ценой.");
                extremum = price;
            }
        }

        return StrategyDecision.pass();
    }

    @Override
    public Instrument getInstrument() {
        return this.operatingInstrument;
    }

    private void checkCanTrade(final TradingState.InstrumentInfo instrumentInfo) {
        if (!Objects.isNull(instrumentInfo)) {
            final var newCanTrade = instrumentInfo.canTrade;
            if (newCanTrade != this.canTrade)
                logger.fine("Изменился торговый статус инструмента: " + instrumentInfo.canTrade);
            this.canTrade = newCanTrade;
        }
    }

    private StrategyDecision placeLimitOrder(final BigDecimal price, final TradingState.Order.Type operationType) {
        initialPrice = price;
        extremum = price;

        final BigDecimal correctedPrice = price.divide(operatingInstrument.minPriceIncrement, RoundingMode.HALF_DOWN)
                .setScale(0, RoundingMode.DOWN)
                .multiply(operatingInstrument.minPriceIncrement);

        final TradingState.Position currentValue = currentState.currencies
                .getOrDefault(operatingInstrument.currency, TradingState.Position.empty);

        final BigDecimal lotSize = BigDecimal.valueOf(operatingInstrument.lot);
        final int lots;
        if (operationType == TradingState.Order.Type.Buy) {
            final var maxValue = maxOperationValue.compareTo(currentValue.balance) > 0
                    ? currentValue.balance
                    : maxOperationValue;
            lots = maxValue.divideToIntegralValue(
                    correctedPrice.multiply(lotSize))
                    .intValue();
        } else {
            lots = currentState.positions.get(operatingInstrument.figi).balance.divide(lotSize, RoundingMode.DOWN).intValue();
        }

        currentState.waitingForPlacingOrder = true; // TODO hmmm...

        return StrategyDecision.placeLimitOrder(
                operatingInstrument.figi,
                lots,
                operationType,
                correctedPrice
        );
    }
}
