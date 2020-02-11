package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.market.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения рыночной информации.
 */
public interface MarketContext extends Context {

    /**
     * Получение списка акций, доступных для торговли.
     */
    @NotNull
    CompletableFuture<InstrumentsList> getMarketStocks();

    /**
     * Получение списка бондов, доступных для торговли.
     */
    @NotNull
    CompletableFuture<InstrumentsList> getMarketBonds();

    /**
     * Получение списка фондов, доступных для торговли.
     */
    @NotNull
    CompletableFuture<InstrumentsList> getMarketEtfs();

    /**
     * Получение списка валют, доступных для торговли.
     */
    @NotNull
    CompletableFuture<InstrumentsList> getMarketCurrencies();

    /**
     * Получение текущего состояния торгового "стакана".
     *
     * @param figi     Идентификатор инструмента.
     * @param depth    Глубина стакана.
     */
    @NotNull
    CompletableFuture<Optional<Orderbook>> getMarketOrderbook(@NotNull String figi, int depth);

    /**
     * Получение исторических данных по свечам.
     *
     * @param figi     Идентификатор инструмента.
     * @param from     Начальный момент рассматриваемого отрезка временного интервала.
     * @param to       Конечный момент рассматриваемого отрезка временного интервала.
     * @param interval Разрешающий интервал свечей.
     */
    @NotNull
    CompletableFuture<Optional<HistoricalCandles>> getMarketCandles(@NotNull String figi,
                                                                    @NotNull OffsetDateTime from,
                                                                    @NotNull OffsetDateTime to,
                                                                    @NotNull CandleInterval interval);

    /**
     * Поиск инструментов по тикеру.
     *
     * @param ticker   Искомый тикер.
     */
    @NotNull
    CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(@NotNull String ticker);

    /**
     * Поиск инструмента по идентификатору.
     *
     * @param figi     Искомый тикер.
     */
    @NotNull
    CompletableFuture<Optional<Instrument>> searchMarketInstrumentByFigi(@NotNull String figi);

}
