package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.operations.OperationsList;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части касающейся получения информации об операциях.
 */
public interface OperationsContext extends Context {

    /**
     * Получение списка прошедших операций по заданному инструменту за определённый промежуток времени.
     *
     * @param from Дата/время начала промежутка времени.
     * @param to Дата/время конца промежутка времени.
     * @param figi Идентификатор инструмента. Может быть пустым или null.
     */
    @NotNull
    CompletableFuture<OperationsList> getOperations(@NotNull OffsetDateTime from,
                                                    @NotNull OffsetDateTime to,
                                                    @NotNull String figi);

}
