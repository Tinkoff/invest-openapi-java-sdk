package ru.tinkoff.trading.openapi.wrapper.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.tinkoff.trading.openapi.wrapper.Connection;
import ru.tinkoff.trading.openapi.wrapper.Context;
import ru.tinkoff.trading.openapi.data.*;
import ru.tinkoff.trading.openapi.exceptions.OpenApiException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Level;
import java.util.logging.Logger;

class ContextImpl implements Context {

    private static final String ORDERS_PATH = "/orders";
    private static final String ORDERS_LIMITORDER_PATH = "/orders/limit-order";
    private static final String ORDERS_CANCEL_PATH = "/orders/cancel";
    private static final String PORTFOLIO_PATH = "/portfolio";
    private static final String PORTFOLIO_CURRENCIES_PATH = "/portfolio/currencies";
    private static final String MARKET_STOCKS_PATH = "/market/stocks";
    private static final String MARKET_BONDS_PATH = "/market/bonds";
    private static final String MARKET_ETFS_PATH = "/market/etfs";
    private static final String MARKET_CURRENCIES_PATH = "/market/currencies";
    private static final String MARKET_SEARCH_BYTICKER_PATH = "/market/search/by-ticker";
    private static final String MARKET_SEARCH_BYFIGI_PATH = "/market/search/by-figi";
    private static final String OPERATIONS_PATH = "/operations";

    private static final String NOT_FOUND_MESSAGE_CODE = "ACCESS_DENIED";

    private static final TypeReference<OpenApiResponse<OpenApiException>> openApiExceptionTypeReference =
            new TypeReference<>(){};

    private final Connection connection;
    private final SubmissionPublisher<StreamingEvent> streaming;
    private final Logger logger;

    protected static class EmptyPayload {
    }

    private static class LimitOrderDto {
        public final int lots;
        public final OperationType operation;
        public final BigDecimal price;

        LimitOrderDto(LimitOrder limitOrder) {
            this.lots = limitOrder.getLots();
            this.operation = limitOrder.getOperation();
            this.price = limitOrder.getPrice();
        }
    }

    ContextImpl(Connection connection, Logger logger) {
        this.connection = connection;
        this.streaming = new SubmissionPublisher<>();
        this.connection.getListener().setMessageHandler(this::handleStreamMessage);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<List<Order>> getOrders() {
        return sendGetRequest(ORDERS_PATH, new TypeReference<OpenApiResponse<List<Order>>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<PlacedLimitOrder> placeLimitOrder(LimitOrder limitOrder) {
        final var payload = new LimitOrderDto(limitOrder);
        final var pathWithParam = ORDERS_LIMITORDER_PATH + "?figi="
                + URLEncoder.encode(limitOrder.getFigi(), StandardCharsets.UTF_8);
        return sendPostRequest(pathWithParam, payload, new TypeReference<OpenApiResponse<PlacedLimitOrder>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<Void> cancelOrder(String orderId) {
        final var pathWithParam = ORDERS_CANCEL_PATH + "?orderId=" +
                URLEncoder.encode(orderId, StandardCharsets.UTF_8);
        return sendPostRequest(pathWithParam, null, new TypeReference<OpenApiResponse<EmptyPayload>>(){})
                .thenApply(oar -> null);
    }

    @Override
    public CompletableFuture<Portfolio> getPortfolio() {
        return sendGetRequest(PORTFOLIO_PATH, new TypeReference<OpenApiResponse<Portfolio>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies() {
        return sendGetRequest(PORTFOLIO_CURRENCIES_PATH, new TypeReference<OpenApiResponse<PortfolioCurrencies>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<InstrumentsList> getMarketStocks() {
        return sendGetRequest(MARKET_STOCKS_PATH, new TypeReference<OpenApiResponse<InstrumentsList>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<InstrumentsList> getMarketBonds() {
        return sendGetRequest(MARKET_BONDS_PATH, new TypeReference<OpenApiResponse<InstrumentsList>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<InstrumentsList> getMarketEtfs() {
        return sendGetRequest(MARKET_ETFS_PATH, new TypeReference<OpenApiResponse<InstrumentsList>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<InstrumentsList> getMarketCurrencies() {
        return sendGetRequest(MARKET_CURRENCIES_PATH, new TypeReference<OpenApiResponse<InstrumentsList>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(String ticker) {
        final var pathWithParam = MARKET_SEARCH_BYTICKER_PATH + "?ticker=" +
                URLEncoder.encode(ticker, StandardCharsets.UTF_8);
        return sendGetRequest(pathWithParam, new TypeReference<OpenApiResponse<InstrumentsList>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public CompletableFuture<Optional<Instrument>> searchMarketInstrumentByFigi(String figi) {
        final var pathWithParam = MARKET_SEARCH_BYFIGI_PATH + "?figi=" +
                URLEncoder.encode(figi, StandardCharsets.UTF_8);
        return sendGetRequest(pathWithParam, new TypeReference<OpenApiResponse<Instrument>>(){})
                .handle((oar, ex) -> {
                    if (ex == null) {
                        return CompletableFuture.completedFuture(Optional.of(oar.payload));
                    } else {
                        if (ex instanceof OpenApiException &&
                                ((OpenApiException) ex).getCode().equals(NOT_FOUND_MESSAGE_CODE)) {
                            return CompletableFuture.completedFuture(Optional.<Instrument>empty());
                        } else {
                            return CompletableFuture.<Optional<Instrument>>failedFuture(ex);
                        }
                    }
                }).thenCompose(x -> x);
    }

    @Override
    public CompletableFuture<Void> sendStreamingRequest(StreamingRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            final var message = objectMapper.writeValueAsString(request);
            return connection.getWebSocket().sendText(message, true).thenApply(ws -> null);
        } catch (JsonProcessingException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Override
    public CompletableFuture<OperationsList> getOperations(LocalDate from, OperationInterval interval, String figi) {
        final var pathWithParam = OPERATIONS_PATH + "?from=" +
                URLEncoder.encode(from.toString(), StandardCharsets.UTF_8)
                + "&interval=" + URLEncoder.encode(interval.toParamString(), StandardCharsets.UTF_8)
                + "&figi=" + URLEncoder.encode(figi, StandardCharsets.UTF_8);
        return sendGetRequest(pathWithParam, new TypeReference<OpenApiResponse<OperationsList>>(){})
                .thenApply(oar -> oar.payload);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super StreamingEvent> subscriber) {
        this.streaming.subscribe(subscriber);
    }

    protected <In> CompletableFuture<In> sendGetRequest(String path, TypeReference<In> tr) {
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(connection.getHost() + path))
                .header("Authorization", connection.getAuthToken())
                .GET()   // this is the default
                .build();

        return connection.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> handleResponse(response, tr));
    }

    protected <Out, In> CompletableFuture<In> sendPostRequest(String path, Out payload, TypeReference<In> tr) {
        String body;
        if (payload == null) {
            body = "";
        } else {
            try {
                final var objectMapper = new ObjectMapper();
                body = objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException ex) {
                return CompletableFuture.failedFuture(ex);
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(connection.getHost() + path))
                .header("Authorization", connection.getAuthToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return connection.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> handleResponse(response, tr));
    }

    private <D> CompletableFuture<D> handleResponse(HttpResponse<String> response, TypeReference<D> tr) {
        final var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        try {
            switch (response.statusCode()) {
                case 200:
                    return CompletableFuture.completedFuture(mapper.readValue(response.body(), tr));
                case 401:
                    final var ex401 = new OpenApiException(
                            "You have no access to that resource.",
                            NOT_FOUND_MESSAGE_CODE);
                    return CompletableFuture.failedFuture(ex401);
                default:
                    final var answerBody = mapper.<OpenApiResponse<OpenApiException>>readValue(response.body(), openApiExceptionTypeReference);
                    final var exOther = answerBody.payload;
                    final var message = "Ошибка при исполнении запроса, trackingId = " + answerBody.trackingId;
                    logger.log(Level.WARNING, message, exOther);
                    return CompletableFuture.failedFuture(exOther);
            }
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    private void handleStreamMessage(String message) {
        try {
            final var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            final var event = mapper.<StreamingEvent>readValue(message, new TypeReference<StreamingEvent>(){});
            streaming.submit(event);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "При обработке собыйтия из WebSocket что-то произошло.", ex);
        }
    }
}
