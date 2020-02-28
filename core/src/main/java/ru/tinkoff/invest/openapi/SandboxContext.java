package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.models.sandbox.PositionBalance;
import ru.tinkoff.invest.openapi.models.user.BrokerAccountType;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части касающейся режима "песочницы".
 */
public interface SandboxContext extends Context {

    /**
     * Регистрация в системе "песочницы". Проводится один раз для клиента.
     *
     * @param brokerAccountType Тип брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> performRegistration(@Nullable BrokerAccountType brokerAccountType);

    /**
     * Установка значения валютного актива.
     *
     * @param data Жалаемые параметры позиции.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> setCurrencyBalance(@NotNull CurrencyBalance data, @Nullable String brokerAccountId);

    /**
     * Установка позиции по инструменту.
     *
     * @param data Жалаемые параметры позиции.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> setPositionBalance(@NotNull PositionBalance data, @Nullable String brokerAccountId);

    /**
     * Сброс всех установленных значений по активам.
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> clearAll(@Nullable String brokerAccountId);

}
