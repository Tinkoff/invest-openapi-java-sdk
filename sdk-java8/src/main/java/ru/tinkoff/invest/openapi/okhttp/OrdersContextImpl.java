package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.exceptions.NotEnoughBalanceException;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.exceptions.OrderAlreadyCancelledException;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.orders.LimitOrder;
import ru.tinkoff.invest.openapi.model.orders.Order;
import ru.tinkoff.invest.openapi.model.orders.PlacedLimitOrder;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class OrdersContextImpl extends BaseContextImpl implements OrdersContext {

    private static final String NOT_ENOUGH_BALANCE_CODE = "NOT_ENOUGH_BALANCE";
    private static final String ORDER_ERROR_CODE = "ORDER_ERROR";

    private static final TypeReference<RestResponse<List<Order>>> listOrderTypeReference =
            new TypeReference<RestResponse<List<Order>>>() {
            };
    private static final TypeReference<RestResponse<PlacedLimitOrder>> placedLimitOrderTypeReference =
            new TypeReference<RestResponse<PlacedLimitOrder>>() {
            };

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
    public void getOrders(@NotNull final Consumer<List<Order>> onComplete, @NotNull final Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final RestResponse<List<Order>> result = handleResponse(response, listOrderTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(NOT_ENOUGH_BALANCE_CODE)) {
                        onError.accept(new NotEnoughBalanceException(ex.getMessage(), ex.getCode()));
                    } else {
                        onError.accept(ex);
                    }
                }
            }
        });
    }

    @Override
    public void placeLimitOrder(@NotNull final LimitOrder limitOrder, @NotNull final Consumer<PlacedLimitOrder> onComplete, @NotNull final Consumer<Throwable> onError) {
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(limitOrder);
        } catch (JsonProcessingException ex) {
            onError.accept(ex);
            return;
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("limit-order")
                .addQueryParameter("figi", limitOrder.figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    final RestResponse<PlacedLimitOrder> result = handleResponse(response, placedLimitOrderTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void cancelOrder(@NotNull final String orderId, @NotNull final Consumer<Void> onComplete, @NotNull final Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
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
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    onComplete.accept(null);
                } catch (OpenApiException ex) { // TODO what if orderId doesn't exist?
                    if (ex.getCode().equals(ORDER_ERROR_CODE)) {
                        onError.accept(new OrderAlreadyCancelledException(ex.getMessage(), ex.getCode()));
                    } else {
                        onError.accept(ex);
                    }
                }
            }
        });
    }

}
