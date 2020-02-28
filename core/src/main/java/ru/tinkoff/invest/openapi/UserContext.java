package ru.tinkoff.invest.openapi;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import ru.tinkoff.invest.openapi.models.user.AccountsList;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения информации о клиенте.
 */
public interface UserContext extends Context {

    /**
     * Асинхронное получение списка брокерских счетов.
     * 
     * @return Список счетов.
     */
    @NotNull
    CompletableFuture<AccountsList> getAccounts();

}
