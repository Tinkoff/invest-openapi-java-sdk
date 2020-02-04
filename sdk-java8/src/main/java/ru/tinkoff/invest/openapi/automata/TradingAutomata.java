package ru.tinkoff.invest.openapi.automata;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OpenApiFactoryBase;
import ru.tinkoff.invest.openapi.model.market.CandleInterval;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TradingAutomata implements AutoCloseable {

    private final ReactiveApi apiProcessor;
    private final StrategyProcessor strategyProcessor;

    private boolean isRunning;

    public TradingAutomata(@NotNull final OpenApiFactoryBase apiFactory,
                           @NotNull final Executor executor,
                           @NotNull final Logger logger) {
        this.apiProcessor = new ReactiveApi(executor, apiFactory, logger);
        this.strategyProcessor = new StrategyProcessor(executor, logger);

        this.apiProcessor.subscribe(strategyProcessor);
        this.strategyProcessor.subscribe(apiProcessor);

        this.isRunning = false;
    }

    public boolean operating() {
        return this.isRunning;
    }

    @NotNull
    public OpenApi api() {
        return apiProcessor.api;
    }

    public synchronized void start() {
        if (this.isRunning) return;

        this.strategyProcessor.strategies().forEach(this::prepareStrategy);

        this.isRunning = true;
    }

    public synchronized void stop() {
        if (!this.isRunning) return;

        this.strategyProcessor.strategies().forEach(this::cleanupStrategy);

        this.isRunning = false;
    }

    public synchronized void addStrategy(final Strategy strategy) {
        this.strategyProcessor.addStrategy(strategy);

        if (this.isRunning) this.prepareStrategy(strategy);
    }

    public synchronized void removeStrategy(final Strategy strategy) {
        this.strategyProcessor.removeStrategy(strategy);

        if (this.isRunning) this.cleanupStrategy(strategy);
    }

    private void prepareStrategy(@NotNull final Strategy strategy) {
        final String figi = strategy.getInstrument().figi;
        final InputApiSignal.StartInstrumentInfoStreaming instrumentInfoSignal =
                new InputApiSignal.StartInstrumentInfoStreaming(figi);
        final CandleInterval ci = EntitiesAdaptor.convertTradingStateToApiEntity(strategy.getCandleInterval());
        final InputApiSignal.StartCandlesStreaming candlesSignal =
                new InputApiSignal.StartCandlesStreaming(figi, ci);
        final InputApiSignal.StartOrderbookStreaming orderbookSignal =
                new InputApiSignal.StartOrderbookStreaming(figi, strategy.getOrderbookDepth());

        this.apiProcessor.onNext(instrumentInfoSignal);
        this.apiProcessor.onNext(candlesSignal);
        this.apiProcessor.onNext(orderbookSignal);
    }

    private void cleanupStrategy(@NotNull final Strategy strategy) {
        final String figi = strategy.getInstrument().figi;
        final InputApiSignal.StopInstrumentInfoStreaming instrumentInfoSignal =
                new InputApiSignal.StopInstrumentInfoStreaming(figi);
        final CandleInterval ci = EntitiesAdaptor.convertTradingStateToApiEntity(strategy.getCandleInterval());
        final InputApiSignal.StopCandlesStreaming candlesSignal =
                new InputApiSignal.StopCandlesStreaming(figi, ci);
        final InputApiSignal.StopOrderbookStreaming orderbookSignal =
                new InputApiSignal.StopOrderbookStreaming(figi, strategy.getOrderbookDepth());

        this.apiProcessor.onNext(instrumentInfoSignal);
        this.apiProcessor.onNext(candlesSignal);
        this.apiProcessor.onNext(orderbookSignal);
    }

    @Override
    public void close() throws Exception {
        this.stop();
        this.api().streamingContext.close();
    }
}
