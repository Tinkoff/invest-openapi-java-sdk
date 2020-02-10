package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.models.sandbox.PositionBalance;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части касающейся режима "песочницы".
 */
public interface SandboxContext extends Context {

    /**
     * Регистрация в системе "песочницы". Проводится один раз для клиента.
     */
    @NotNull
    CompletableFuture<Void> performRegistration();

    /**
     * Установка значения валютного актива.
     *
     * @param data Жалаемые параметры позиции.
     */
    @NotNull
    CompletableFuture<Void> setCurrencyBalance(@NotNull CurrencyBalance data);

    /**
     * Установка позиции по инструменту.
     *
     * @param data Жалаемые параметры позиции.
     */
    @NotNull
    CompletableFuture<Void> setPositionBalance(@NotNull PositionBalance data);

    /**
     * Сброс всех установленных значений по активам.
     */
    @NotNull
    CompletableFuture<Void> clearAll();

}
