package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.model.operations.OperationsList;

import java.time.OffsetDateTime;
import java.util.function.BiConsumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся заявок.
 */
public interface OperationsContext extends Context {

    /**
     * Получение списка прошедших операций по заданному инструменту за определённый промежуток времени.
     *
     * @param from Дата/время начала промежутка времени.
     * @param to Дата/время конца промежутка времени.
     * @param figi Идентификатор инструмента. Может быть пустым или null.
     * @param callback Функция обратного вызова.
     */
    void getOperations(OffsetDateTime from,
                       OffsetDateTime to,
                       String figi,
                       BiConsumer<OperationsList, Throwable> callback);

}
