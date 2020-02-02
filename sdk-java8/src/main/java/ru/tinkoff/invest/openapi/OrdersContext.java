package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.orders.LimitOrder;
import ru.tinkoff.invest.openapi.model.orders.Order;
import ru.tinkoff.invest.openapi.model.orders.PlacedLimitOrder;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся заявок.
 */
public interface OrdersContext extends Context {

    /**
     * Получение списка активных заявок.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void getOrders(@NotNull Consumer<List<Order>> onComplete, @NotNull Consumer<Throwable> onError);

    /**
     * Размещение лимитной заявки.
     *
     * @param limitOrder Параметры отправляемой заявки.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void placeLimitOrder(@NotNull LimitOrder limitOrder,
                         @NotNull Consumer<PlacedLimitOrder> onComplete,
                         @NotNull Consumer<Throwable> onError);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId Идентификатор заявки.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void cancelOrder(@NotNull String orderId, @NotNull Consumer<Void> onComplete, @NotNull Consumer<Throwable> onError);

}
