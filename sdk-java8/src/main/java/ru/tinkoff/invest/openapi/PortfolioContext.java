package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;

import java.util.function.Consumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения информации о состоянии портфеля.
 */
public interface PortfolioContext extends Context {

    /**
     * Получение информации по портфелю инструментов.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getPortfolio(@NotNull Consumer<Portfolio> onComplete,
                      @NotNull Consumer<Throwable> onError);

    /**
     * Получение информации по валютным активам.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getPortfolioCurrencies(@NotNull Consumer<PortfolioCurrencies> onComplete,
                                @NotNull Consumer<Throwable> onError);

}
