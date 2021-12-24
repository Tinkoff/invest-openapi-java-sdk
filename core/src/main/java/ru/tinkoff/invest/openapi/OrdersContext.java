package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.rest.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части, касающейся заявок.
 */
public interface OrdersContext extends Context {

    /**
     * Асинхронное получение списка активных заявок.
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Список заявок.
     */
    @NotNull
    CompletableFuture<List<Order>> getOrders(@Nullable String brokerAccountId);

    /**
     * Размещение лимитной заявки.
     *
     * @param figi Идентификатор инструмента.
     * @param limitOrder Параметры отправляемой заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Размещённая заявка.
     */
    @NotNull
    CompletableFuture<PlacedLimitOrder> placeLimitOrder(@NotNull String figi,
                                                        @NotNull LimitOrderRequest limitOrder,
                                                        @Nullable String brokerAccountId);

    /**
     * Размещение рыночной заявки.
     *
     * @param figi Идентификатор инструмента.
     * @param marketOrder Параметры отправляемой заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Размещённая заявка.
     */
    @NotNull
    CompletableFuture<PlacedMarketOrder> placeMarketOrder(@NotNull String figi,
                                                          @NotNull MarketOrderRequest marketOrder,
                                                          @Nullable String brokerAccountId);

    /**
     * Отзыв лимитной заявки.
     *
     * @param orderId Идентификатор заявки.
     * @param brokerAccountId Идентификатор брокерского счёта.
     * 
     * @return Ничего.
     */
    @NotNull
    CompletableFuture<Void> cancelOrder(@NotNull String orderId, @Nullable String brokerAccountId);

}
