package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.model.market.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся заявок.
 */
public interface MarketContext extends Context {

    /**
     * Получение списка акций, доступных для торговли.
     *
     * @param callback Функция обратного вызова.
     */
    void getMarketStocks(BiConsumer<InstrumentsList, Throwable> callback);

    /**
     * Получение списка бондов, доступных для торговли.
     *
     * @param callback Функция обратного вызова.
     */
    void getMarketBonds(BiConsumer<InstrumentsList, Throwable> callback);

    /**
     * Получение списка фондов, доступных для торговли.
     *
     * @param callback Функция обратного вызова.
     */
    void getMarketEtfs(BiConsumer<InstrumentsList, Throwable> callback);

    /**
     * Получение списка валют, доступных для торговли.
     *
     * @param callback Функция обратного вызова.
     */
    void getMarketCurrencies(BiConsumer<InstrumentsList, Throwable> callback);

    /**
     * Получение текущего состояния торгового "стакана".
     *
     * @param figi     Идентификатор инструмента.
     * @param depth    Глубина стакана.
     * @param callback Функция обратного вызова.
     */
    void getMarketOrderbook(String figi, int depth, BiConsumer<Optional<Orderbook>, Throwable> callback);

    /**
     * Получение исторических данных по свечам.
     *
     * @param figi     Идентификатор инструмента.
     * @param from     Начальный момент рассматриваемого отрезка временного интервала.
     * @param to       Конечный момент рассматриваемого отрезка временного интервала.
     * @param interval Разрешающий интервал свечей.
     * @param callback Функция обратного вызова.
     */
    void getMarketCandles(String figi,
                          OffsetDateTime from,
                          OffsetDateTime to,
                          CandleInterval interval,
                          BiConsumer<Optional<HistoricalCandles>, Throwable> callback);

    /**
     * Поиск инструментов по тикеру.
     *
     * @param ticker   Искомый тикер.
     * @param callback Функция обратного вызова.
     */
    void searchMarketInstrumentsByTicker(String ticker,
                                         BiConsumer<InstrumentsList, Throwable> callback);

    /**
     * Поиск инструмента по идентификатору.
     *
     * @param figi     Искомый тикер.
     * @param callback Функция обратного вызова.
     */
    void searchMarketInstrumentByFigi(String figi,
                                      BiConsumer<Optional<Instrument>, Throwable> callback);

}
