package ru.tinkoff.trading.openapi.wrapper;

import ru.tinkoff.trading.openapi.data.Currency;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI dВ режиме "песочницы".
 */
public interface SandboxContext extends Context {

    /**
     * Регистрация в системе "песочницы". Проводится один раз для клиента.
     */
    CompletableFuture<Void> performRegistration();

    /**
     * Установка значения валютного актива.
     *
     * @param currency Валюта.
     * @param balance Желаемое значение.
     */
    CompletableFuture<Void> setCurrencyBalance(Currency currency, BigDecimal balance);

    /**
     * Установка позиции по инструментну.
     *
     * @param figi Идентификатор инструмента.
     * @param balance Размер позици.
     */
    CompletableFuture<Void> setPositionBalance(String figi, BigDecimal balance);

    /**
     * Сброс всех установленных значений по активам.
     */
    CompletableFuture<Void> clearAll();


}
