package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.exceptions.NotEnoughBalanceException;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.exceptions.OrderAlreadyCancelledException;
import ru.tinkoff.invest.openapi.models.RestResponse;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class OrdersContextImpl extends BaseContextImpl implements OrdersContext {

    private static final String NOT_ENOUGH_BALANCE_CODE = "NOT_ENOUGH_BALANCE";
    private static final String ORDER_ERROR_CODE = "ORDER_ERROR";

    private static final TypeReference<RestResponse<List<Order>>> listOrderTypeReference =
            new TypeReference<RestResponse<List<Order>>>() {};
    private static final TypeReference<RestResponse<PlacedOrder>> placedOrderTypeReference =
            new TypeReference<RestResponse<PlacedOrder>>() {};

    public OrdersContextImpl(@NotNull final OkHttpClient client,
                             @NotNull final String url,
                             @NotNull final String authToken,
                             @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @NotNull
    @Override
    public String getPath() {
        return "orders";
    }

    @Override
    @NotNull
    public CompletableFuture<List<Order>> getOrders(@Nullable final String brokerAccountId) {
        final CompletableFuture<List<Order>> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final RestResponse<List<Order>> result = handleResponse(response, listOrderTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(NOT_ENOUGH_BALANCE_CODE)) {
                        future.completeExceptionally(new NotEnoughBalanceException(ex.getMessage(), ex.getCode()));
                    } else {
                        future.completeExceptionally(ex);
                    }
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<PlacedOrder> placeLimitOrder(@NotNull final String figi,
                                                          @NotNull final LimitOrder limitOrder,
                                                          @Nullable final String brokerAccountId) {
        final CompletableFuture<PlacedOrder> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(limitOrder);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("limit-order")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    final RestResponse<PlacedOrder> result = handleResponse(response, placedOrderTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<PlacedOrder> placeMarketOrder(@NotNull final String figi,
                                                           @NotNull final MarketOrder marketOrder,
                                                           @Nullable final String brokerAccountId) {
        final CompletableFuture<PlacedOrder> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(marketOrder);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("market-order")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    final RestResponse<PlacedOrder> result = handleResponse(response, placedOrderTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> cancelOrder(@NotNull final String orderId,
                                               @Nullable final String brokerAccountId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("cancel")
                .addQueryParameter("orderId", orderId)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(ORDER_ERROR_CODE)) {
                        future.completeExceptionally(new OrderAlreadyCancelledException(ex.getMessage(), ex.getCode()));
                    } else {
                        future.completeExceptionally(ex);
                    }
                }
            }
        });

        return future;
    }

}
