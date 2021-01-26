package ru.tinkoff.invest.openapi;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import ru.tinkoff.invest.openapi.model.rest.UserAccounts;

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
    CompletableFuture<UserAccounts> getAccounts();

}
