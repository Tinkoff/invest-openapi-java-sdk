package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import ru.tinkoff.invest.openapi.exceptions.NotEnoughBalanceException;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.model.EmptyPayload;
import ru.tinkoff.invest.openapi.model.ErrorPayload;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.orders.LimitOrder;
import ru.tinkoff.invest.openapi.model.orders.Order;
import ru.tinkoff.invest.openapi.model.orders.PlacedLimitOrder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

final class OrdersContextImpl implements OrdersContext {

    private static final String ACCESS_DENIED_MESSAGE_CODE = "ACCESS_DENIED";
    private static final String NOT_ENOUGH_BALANCE_CODE = "NOT_ENOUGH_BALANCE";
    private static final TypeReference<RestResponse<ErrorPayload>> errorTypeReference =
            new TypeReference<RestResponse<ErrorPayload>>() {
            };
    private static final TypeReference<RestResponse<List<Order>>> listOrderTypeReference =
            new TypeReference<RestResponse<List<Order>>>() {
            };
    private static final TypeReference<RestResponse<EmptyPayload>> emptyPayloadTypeReference =
            new TypeReference<RestResponse<EmptyPayload>>() {
            };
    private static final TypeReference<RestResponse<PlacedLimitOrder>> placedLimitOrderTypeReference =
            new TypeReference<RestResponse<PlacedLimitOrder>>() {
            };
    private final String authToken;
    private final HttpUrl finalUrl;
    private final OkHttpClient client;
    private final Logger logger;
    private final ObjectMapper mapper;

    public OrdersContextImpl(final OkHttpClient client,
                             final String url,
                             final String authToken,
                             final Logger logger) {
        this.authToken = authToken;
        this.finalUrl = Objects.requireNonNull(HttpUrl.parse(url))
                .newBuilder()
                .addPathSegment(this.getPath())
                .build();
        this.client = client;
        this.logger = logger;
        this.mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    @Override
    public String getPath() {
        return "orders";
    }

    @Override
    public void getOrders(Consumer<List<Order>> onComplete, Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<List<Order>> result = handleResponse(response, listOrderTypeReference);

            onComplete.accept(result.payload);
        } catch (OpenApiException ex) {
            if (ex.getCode().equals(NOT_ENOUGH_BALANCE_CODE)) {
                onError.accept(new NotEnoughBalanceException(ex.getMessage(), ex.getCode()));
            } else {
                onError.accept(ex);
            }
        } catch (Exception ex) {
            onError.accept(ex);
        }
    }

    @Override
    public void placeLimitOrder(LimitOrder limitOrder, Consumer<PlacedLimitOrder> onComplete, Consumer<Throwable> onError) {
        String renderedBody = "";
        try {
            renderedBody = mapper.writeValueAsString(limitOrder);
        } catch (JsonProcessingException ex) {
            onError.accept(ex);
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("limit-order")
                .addQueryParameter("figi", limitOrder.figi)
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<PlacedLimitOrder> result = handleResponse(response, placedLimitOrderTypeReference);

            onComplete.accept(result.payload);
        } catch (Exception ex) {
            onError.accept(ex);
        }
    }

    @Override
    public void cancelOrder(String orderId, Consumer<Void> onComplete, Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("cancel")
                .addQueryParameter("orderId", orderId)
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .post(RequestBody.create(new byte[] {}))
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response, emptyPayloadTypeReference);

            onComplete.accept(null);
        } catch (Exception ex) { // TODO what if orderId doesn't exist?
            onError.accept(ex);
        }
    }

    private <D> D handleResponse(Response response, TypeReference<D> tr) throws IOException, OpenApiException {
        switch (response.code()) {
            case 200:
                final InputStream bodyStream = Objects.requireNonNull(response.body()).byteStream();
                return mapper.readValue(bodyStream, tr);
            case 401:
                throw new OpenApiException(
                        "You have no access to that resource.",
                        ACCESS_DENIED_MESSAGE_CODE);
            default:
                final InputStream errorStream = Objects.requireNonNull(response.body()).byteStream();
                final RestResponse<ErrorPayload> answerBody = mapper.readValue(errorStream, errorTypeReference);
                final ErrorPayload error = answerBody.payload;
                final String message = "Ошибка при исполнении запроса, trackingId = " + answerBody.trackingId;
                logger.severe(message);
                throw new OpenApiException(error.message, error.code);
        }
    }

}
