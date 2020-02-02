package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.market.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения рыночной информации.
 */
public interface MarketContext extends Context {

    /**
     * Получение списка акций, доступных для торговли.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getMarketStocks(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError);

    /**
     * Получение списка бондов, доступных для торговли.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getMarketBonds(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError);

    /**
     * Получение списка фондов, доступных для торговли.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getMarketEtfs(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError);

    /**
     * Получение списка валют, доступных для торговли.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getMarketCurrencies(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError);

    /**
     * Получение текущего состояния торгового "стакана".
     *
     * @param figi     Идентификатор инструмента.
     * @param depth    Глубина стакана.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getMarketOrderbook(@NotNull String figi,
                            int depth,
                            @NotNull Consumer<Optional<Orderbook>> onComplete,
                            @NotNull Consumer<Throwable> onError);

    /**
     * Получение исторических данных по свечам.
     *
     * @param figi     Идентификатор инструмента.
     * @param from     Начальный момент рассматриваемого отрезка временного интервала.
     * @param to       Конечный момент рассматриваемого отрезка временного интервала.
     * @param interval Разрешающий интервал свечей.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getMarketCandles(@NotNull String figi,
                          @NotNull OffsetDateTime from,
                          @NotNull OffsetDateTime to,
                          @NotNull CandleInterval interval,
                          @NotNull Consumer<Optional<HistoricalCandles>> onComplete,
                          @NotNull Consumer<Throwable> onError);

    /**
     * Поиск инструментов по тикеру.
     *
     * @param ticker   Искомый тикер.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void searchMarketInstrumentsByTicker(@NotNull String ticker,
                                         @NotNull Consumer<InstrumentsList> onComplete,
                                         @NotNull Consumer<Throwable> onError);

    /**
     * Поиск инструмента по идентификатору.
     *
     * @param figi     Искомый тикер.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void searchMarketInstrumentByFigi(@NotNull String figi,
                                      @NotNull Consumer<Optional<Instrument>> onComplete,
                                      @NotNull Consumer<Throwable> onError);

}
