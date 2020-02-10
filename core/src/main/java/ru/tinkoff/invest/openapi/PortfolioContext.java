package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения информации о состоянии портфеля.
 */
public interface PortfolioContext extends Context {

    /**
     * Получение информации по портфелю инструментов.
     */
    @NotNull
    CompletableFuture<Portfolio> getPortfolio();

    /**
     * Получение информации по валютным активам.
     */
    @NotNull
    CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies();

}
