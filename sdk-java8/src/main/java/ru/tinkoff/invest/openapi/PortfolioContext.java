package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.model.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;

import java.util.function.BiConsumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся заявок.
 */
public interface PortfolioContext extends Context {

    /**
     * Получение информации по портфелю инструментов.
     *
     * @param callback Функция обратного вызова.
     */
    void getPortfolio(BiConsumer<Portfolio, Throwable> callback);

    /**
     * Получение информации по валютным активам.
     *
     * @param callback Функция обратного вызова.
     */
    void getPortfolioCurrencies(BiConsumer<PortfolioCurrencies, Throwable> callback);

}
