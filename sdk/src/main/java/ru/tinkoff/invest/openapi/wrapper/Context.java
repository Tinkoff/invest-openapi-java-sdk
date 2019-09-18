package ru.tinkoff.invest.openapi.wrapper;

import ru.tinkoff.invest.openapi.data.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Интерфейс работы с OpenAPI.
 */
public interface Context extends Flow.Publisher<StreamingEvent> {

    /**
     * Получение списка активных заявок.
     *
     * @return Список заявок.
     */
    CompletableFuture<List<Order>> getOrders();

    /**
     * Размещение лимитной заявки.
     *
     * @param limitOrder Параметры отправляемой заявки.
     * @return Параметры размещённой заявки.
     */
    CompletableFuture<PlacedLimitOrder> placeLimitOrder(LimitOrder limitOrder);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId Идентификатор заявки.
     */
    CompletableFuture<Void> cancelOrder(String orderId);

    /**
     * Получение информации по портфелю инструментов.
     *
     * @return Портфель инструментов.
     */
    CompletableFuture<Portfolio> getPortfolio();

    /**
     * Получение информации по валютным активам.
     *
     * @return Валютные активы.
     */
    CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies();

    /**
     * Получение списка акций, доступных для торговли.
     *
     * @return Список акций.
     */
    CompletableFuture<InstrumentsList> getMarketStocks();

    /**
     * Получение списка бондов, доступных для торговли.
     *
     * @return Список бондов.
     */
    CompletableFuture<InstrumentsList> getMarketBonds();

    /**
     * Получение списка фондов, доступных для торговли.
     *
     * @return Список фондов.
     */
    CompletableFuture<InstrumentsList> getMarketEtfs();

    /**
     * Получение списка валют, доступных для торговли.
     *
     * @return Список валют.
     */
    CompletableFuture<InstrumentsList> getMarketCurrencies();

    /**
     * Поиск инструментов по тикеру.
     *
     * @param ticker Искомый тикер.
     * @return Список найденых иснтрументов.
     */
    CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(String ticker);

    /**
     * Поиск инструмента по тикеру.
     *
     * @param figi Искомый тикер.
     * @return Возможно найденный иснтрумент.
     */
    CompletableFuture<Optional<Instrument>> searchMarketInstrumentByFigi(String figi);

    /**
     * Отправка запроса для потока данных.
     *
     * @param request Запрос.
     */
    CompletableFuture<Void> sendStreamingRequest(StreamingRequest request);

    /**
     * Получение списка прошедших операций по заданному инструменту за определённый промежуток времени.
     *
     * @param from Дата начала промежутка времени.
     * @param interval Продолжительность промежутка времени.
     * @param figi Идентификатор инструмента.
     * @return Список операций.
     */
    CompletableFuture<OperationsList> getOperations(LocalDate from, OperationInterval interval, String figi);

}
