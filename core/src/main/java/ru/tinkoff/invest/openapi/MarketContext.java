package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.rest.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части, касающейся получения рыночной информации.
 */
public interface MarketContext extends Context {

    /**
     * Асинхронное получение списка акций, доступных для торговли.
     * 
     * @return Список акций.
     */
    @NotNull
    CompletableFuture<MarketInstrumentList> getMarketStocks();


    /**
     * Асинхронное получение бондов, доступных для торговли.
     * 
     * @return Список облигаций.
     */
    @NotNull
    CompletableFuture<MarketInstrumentList> getMarketBonds();

    /**
     * Асинхронное получение списка фондов, доступных для торговли.
     * 
     * @return Список фондов.
     */
    @NotNull
    CompletableFuture<MarketInstrumentList> getMarketEtfs();

    /**
     * Асинхронное получение списка валют, доступных для торговли.
     * 
     * @return Список валют.
     */
    @NotNull
    CompletableFuture<MarketInstrumentList> getMarketCurrencies();

    /**
     * Асинхронное получение текущего состояния торгового "стакана".
     *
     * @param figi     Идентификатор инструмента.
     * @param depth    Глубина стакана.
     * 
     * @return "Стакан" по инструменту или ничего, если инструмент не найден.  
     */
    @NotNull
    CompletableFuture<Optional<Orderbook>> getMarketOrderbook(@NotNull String figi, int depth);

    /**
     * Асинхронное получение исторических данных по свечам.
     *
     * @param figi     Идентификатор инструмента.
     * @param from     Начальный момент рассматриваемого отрезка временного интервала.
     * @param to       Конечный момент рассматриваемого отрезка временного интервала.
     * @param interval Разрешающий интервал свечей.
     * 
     * @return Данные по свечам инструмента или ничего, если инструмент не найден.
     */
    @NotNull
    CompletableFuture<Optional<Candles>> getMarketCandles(@NotNull String figi,
                                                          @NotNull OffsetDateTime from,
                                                          @NotNull OffsetDateTime to,
                                                          @NotNull CandleResolution interval);

    /**
     * Асинхронный поиск инструментов по тикеру.
     *
     * @param ticker Искомый тикер.
     * 
     * @return Список инструментов.
     */
    @NotNull
    CompletableFuture<MarketInstrumentList> searchMarketInstrumentsByTicker(@NotNull String ticker);

    /**
     * Асинхронный поиск инструмента по идентификатору.
     *
     * @param figi Искомый тикер.
     * 
     * @return Найденный инструмент или ничего, если инструмент не найден.
     */
    @NotNull
    CompletableFuture<Optional<SearchMarketInstrument>> searchMarketInstrumentByFigi(@NotNull String figi);

}
