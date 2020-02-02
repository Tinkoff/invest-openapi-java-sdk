package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.operations.OperationsList;
import ru.tinkoff.invest.openapi.model.orders.PlacedLimitOrder;

import java.time.OffsetDateTime;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getOperations(@NotNull OffsetDateTime from,
                       @NotNull OffsetDateTime to,
                       @NotNull String figi,
                       @NotNull Consumer<OperationsList> onComplete,
                       @NotNull Consumer<Throwable> onError);

}
