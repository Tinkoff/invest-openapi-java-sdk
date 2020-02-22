package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения информации о состоянии портфеля.
 */
public interface PortfolioContext extends Context {

    /**
     * Получение информации по портфелю инструментов.
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     */
    @NotNull
    CompletableFuture<Portfolio> getPortfolio(@Nullable String brokerAccountId);

    /**
     * Получение информации по валютным активам.
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     */
    @NotNull
    CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies(@Nullable String brokerAccountId);

}
