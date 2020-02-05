package ru.tinkoff.invest.openapi.example;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.automata.TradingState;

import java.math.BigDecimal;
import java.util.Arrays;

public class TradingParameters {
    @NotNull
    public final String ssoToken;
    @NotNull
    public final String[] tickers;
    @NotNull
    public final TradingState.Candle.CandleInterval[] candleIntervals;
    @NotNull
    public final BigDecimal[] maxVolumes;
    public final boolean sandboxMode;

    public TradingParameters(@NotNull final String ssoToken,
                             @NotNull final String[] tickers,
                             @NotNull final TradingState.Candle.CandleInterval[] candleIntervals,
                             @NotNull final BigDecimal[] maxVolumes,
                             final boolean sandboxMode) {
        this.ssoToken = ssoToken;
        this.tickers = tickers;
        this.candleIntervals = candleIntervals;
        this.maxVolumes = maxVolumes;
        this.sandboxMode = sandboxMode;
    }

    @NotNull
    public static TradingParameters fromProgramArgs(@NotNull final String ssoTokenArg,
                                                    @NotNull final String tickersArg,
                                                    @NotNull final String candleIntervalsArg,
                                                    @NotNull final String maxVolumesArg,
                                                    @NotNull final String sandboxModeArg) {
        final var tickers = tickersArg.split(",");
        final var candleIntervals = Arrays.stream(candleIntervalsArg.split(","))
                .map(TradingParameters::parseCandleInterval)
                .toArray(TradingState.Candle.CandleInterval[]::new);
        if (candleIntervals.length != tickers.length)
            throw new IllegalArgumentException("Количество переданных разрешающих интервалов свечей не совпадает с переданным количеством тикеров.");

        final BigDecimal[] maxVolumes = Arrays.stream(maxVolumesArg.split(","))
                .map(BigDecimal::new)
                .toArray(BigDecimal[]::new);
        if (maxVolumes.length != tickers.length)
            throw new IllegalArgumentException("Количество переданных допустимых объёмов используемых средств не совпадает с переданным количеством тикеров.");

        final var useSandbox = Boolean.parseBoolean(sandboxModeArg);

        return new TradingParameters(ssoTokenArg, tickers, candleIntervals, maxVolumes, useSandbox);
    }

    private static TradingState.Candle.CandleInterval parseCandleInterval(final String str) {
        switch (str) {
            case "1min":
                return TradingState.Candle.CandleInterval.ONE_MIN;
            case "2min":
                return TradingState.Candle.CandleInterval.TWO_MIN;
            case "3min":
                return TradingState.Candle.CandleInterval.THREE_MIN;
            case "5min":
                return TradingState.Candle.CandleInterval.FIVE_MIN;
            case "10min":
                return TradingState.Candle.CandleInterval.TEN_MIN;
            default:
                throw new IllegalArgumentException("Не распознан разрешающий интервал!");
        }
    }
}
