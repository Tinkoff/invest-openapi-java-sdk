package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     */
    @NotNull
    CompletableFuture<List<Order>> getOrders(@Nullable String brokerAccountId);

    /**
     * Размещение лимитной заявки.
     *
     * @param figi Идентификатор инструмента.
     * @param limitOrder Параметры отправляемой заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     */
    @NotNull
    CompletableFuture<PlacedLimitOrder> placeLimitOrder(@NotNull String figi,
                                                        @NotNull LimitOrder limitOrder,
                                                        @Nullable String brokerAccountId);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId Идентификатор заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     */
    @NotNull
    CompletableFuture<Void> cancelOrder(@NotNull String orderId, @Nullable String brokerAccountId);

}
