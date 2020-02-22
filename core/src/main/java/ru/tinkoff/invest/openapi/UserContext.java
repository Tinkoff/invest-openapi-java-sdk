package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.operations.OperationsList;
import ru.tinkoff.invest.openapi.models.user.AccountsList;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения информации о клиенте.
 */
public interface UserContext extends Context {

    /**
     * Получение списка брокерских счетов.
     */
    @NotNull
    CompletableFuture<AccountsList> getAccounts();

}
