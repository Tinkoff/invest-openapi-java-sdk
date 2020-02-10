package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedLimitOrder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся заявок.
 */
public interface OrdersContext extends Context {

    /**
     * Получение списка активных заявок.
     */
    @NotNull
    CompletableFuture<List<Order>> getOrders();

    /**
     * Размещение лимитной заявки.
     *
     * @param figi Идентификатор инструмента.
     * @param limitOrder Параметры отправляемой заявки.
     */
    @NotNull
    CompletableFuture<PlacedLimitOrder> placeLimitOrder(@NotNull String figi,
                                                        @NotNull LimitOrder limitOrder);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId Идентификатор заявки.
     */
    @NotNull
    CompletableFuture<Void> cancelOrder(@NotNull String orderId);

}
