package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.rest.SandboxRegisterRequest;
import ru.tinkoff.invest.openapi.model.rest.SandboxSetCurrencyBalanceRequest;
import ru.tinkoff.invest.openapi.model.rest.SandboxSetPositionBalanceRequest;

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
    CompletableFuture<Void> performRegistration(@NotNull SandboxRegisterRequest registerRequest);

    /**
     * Установка значения валютного актива.
     *
     * @param data Жалаемые параметры позиции.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> setCurrencyBalance(@NotNull SandboxSetCurrencyBalanceRequest balanceRequest,
                                               @Nullable String brokerAccountId);

    /**
     * Установка позиции по инструменту.
     *
     * @param data Жалаемые параметры позиции.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> setPositionBalance(@NotNull SandboxSetPositionBalanceRequest balanceRequest,
                                               @Nullable String brokerAccountId);

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
