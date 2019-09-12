package ru.tinkoff.invest.openapi.wrapper.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.Context;
import ru.tinkoff.invest.openapi.data.*;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.testkit.TestableListener;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

class ContextImplTest {

    private static final String host = "http://localhost/openapi-gw";
    private static final String token = "super_token";
    private static Context context;
    private static HttpClient httpClient;
    private static WebSocket webSocket;
    private static TestableListener listener = new TestableListener();

    private abstract static class HttpStringResponse implements HttpResponse<String> {}

    @BeforeAll
    static void initTest() {
        final var logger = Logger.getLogger(ContextImplTest.class.getName());

        httpClient = mock(HttpClient.class);
        webSocket = mock(WebSocket.class);
        final Connection connection = mock(ConnectionImpl.class);
        when(connection.getHost()).thenReturn(host);
        when(connection.getAuthToken()).thenReturn(token);
        when(connection.getHttpClient()).thenReturn(httpClient);
        when(connection.getWebSocket()).thenReturn(webSocket);
        when(connection.getListener()).thenReturn(listener);

        context = new ContextImpl(connection, logger);
    }

    @Test
    void gettingOrders() throws ExecutionException, InterruptedException {
        final var someOrder = new Order(
            "id",
            "figi",
            OperationType.Buy,
            OrderStatus.Fill,
            10,
            10,
            OrderType.Market,
            BigDecimal.valueOf(100)
        );
        final var expectedOrders = List.of(someOrder);

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": [{" +
                    "\"id\":\"id\"," +
                    "\"figi\":\"figi\"," +
                    "\"operation\":\"Buy\"," +
                    "\"status\":\"Fill\"," +
                    "\"requestedLots\":10," +
                    "\"executedLots\":10," +
                    "\"type\":\"Market\"," +
                    "\"price\":100" +
                "}]" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getOrders().get();
        assertEquals(actualResponse.size(), expectedOrders.size());
        final var order = actualResponse.get(0);
        assertEquals(order.getId(), someOrder.getId());
        assertEquals(order.getFigi(), someOrder.getFigi());
        assertEquals(order.getOperation(), someOrder.getOperation());
        assertEquals(order.getStatus(), someOrder.getStatus());
        assertEquals(order.getRequestedLots(), someOrder.getRequestedLots());
        assertEquals(order.getExecutedLots(), someOrder.getExecutedLots());
        assertEquals(order.getType(), someOrder.getType());
        assertEquals(order.getPrice(), someOrder.getPrice());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/orders"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void settingCurrencyBalance() throws ExecutionException, InterruptedException {
        final var someLimitOrder = new LimitOrder("figi",10, OperationType.Buy, BigDecimal.valueOf(1000));

        final var expectedOrder = new PlacedLimitOrder(
                "id",
                someLimitOrder.getOperation(),
                "status",
                "",
                someLimitOrder.getLots(),
                someLimitOrder.getLots(),
                new MoneyAmount(Currency.RUB, BigDecimal.ZERO)
        );

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                    "\"orderId\": \"id\"," +
                    "\"operation\": \"Buy\"," +
                    "\"status\": \"status\"," +
                    "\"rejectReason\": \"\"," +
                    "\"requestedLots\": 10," +
                    "\"executedLots\": 10," +
                    "\"commission\": {" +
                        "\"currency\": \"RUB\"," +
                        "\"value\": 0" +
                    "}" +
                "}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var result = context.placeLimitOrder(someLimitOrder).get();
        assertEquals(result.getId(), expectedOrder.getId());
        assertEquals(result.getOperation(), expectedOrder.getOperation());
        assertEquals(result.getStatus(), expectedOrder.getStatus());
        assertEquals(result.getRejectReason(), expectedOrder.getRejectReason());
        assertEquals(result.getRequestedLots(), expectedOrder.getRequestedLots());
        assertEquals(result.getExecutedLots(), expectedOrder.getExecutedLots());
        assertEquals(result.getCommission().getCurrency(), expectedOrder.getCommission().getCurrency());
        assertEquals(result.getCommission().getValue(), expectedOrder.getCommission().getValue());

        final var requestBody = "{\"lots\":10,\"operation\":\"Buy\",\"price\":1000}";
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/orders/limit-order?figi=" + someLimitOrder.getFigi()))
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void cancellingOrder() throws ExecutionException, InterruptedException {
        final var someOrderId = "orderId";

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var result = context.cancelOrder(someOrderId).get();
        assertNull(result);

        final var requestBody = "";
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/orders/cancel?orderId=" + someOrderId))
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingPortfolio() throws ExecutionException, InterruptedException {
        final var somePortfolioPosition = new Portfolio.PortfolioPosition(
                "figi",
                "ticker",
                "isin",
                InstrumentType.Stock,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100),
                new MoneyAmount(Currency.RUB, BigDecimal.TEN),
                1
        );
        final var expectedPortfolio = new Portfolio(List.of(somePortfolioPosition));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {\"positions\":[{" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"instrumentType\":\"Stock\"," +
                "\"balance\":1000," +
                "\"blocked\":100," +
                "\"expectedYield\":{\"currency\":\"RUB\",\"value\":10}," +
                "\"lots\":1" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getPortfolio().get();
        assertEquals(actualResponse.getPositions().size(), expectedPortfolio.getPositions().size());
        final var position = actualResponse.getPositions().get(0);
        assertEquals(position.getFigi(), somePortfolioPosition.getFigi());
        assertEquals(position.getTicker(), somePortfolioPosition.getTicker());
        assertEquals(position.getIsin(), somePortfolioPosition.getIsin());
        assertEquals(position.getInstrumentType(), somePortfolioPosition.getInstrumentType());
        assertEquals(position.getBalance(), somePortfolioPosition.getBalance());
        assertEquals(position.getBlocked(), somePortfolioPosition.getBlocked());
        assertEquals(position.getExpectedYield().getValue(), somePortfolioPosition.getExpectedYield().getValue());
        assertEquals(position.getExpectedYield().getCurrency(), somePortfolioPosition.getExpectedYield().getCurrency());
        assertEquals(position.getLots(), somePortfolioPosition.getLots());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/portfolio"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingPortfolioCurrencies() throws ExecutionException, InterruptedException {
        final var somePortfolioCurrency = new PortfolioCurrencies.PortfolioCurrency(
                Currency.RUB,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100)
        );
        final var expectedPortfolioCurrencies = new PortfolioCurrencies(List.of(somePortfolioCurrency));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {\"currencies\":[{" +
                "\"currency\":\"RUB\"," +
                "\"balance\":1000," +
                "\"blocked\":100" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getPortfolioCurrencies().get();
        assertEquals(actualResponse.getCurrencies().size(), expectedPortfolioCurrencies.getCurrencies().size());
        final var position = actualResponse.getCurrencies().get(0);
        assertEquals(position.getCurrency(), somePortfolioCurrency.getCurrency());
        assertEquals(position.getBalance(), somePortfolioCurrency.getBalance());
        assertEquals(position.getBlocked(), somePortfolioCurrency.getBlocked());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/portfolio/currencies"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingMarketStocks() throws ExecutionException, InterruptedException {
        final var someStock = new Instrument(
                "figi",
                "ticker",
                "isin",
                BigDecimal.TEN,
                1,
                Currency.RUB
        );
        final var expectedStocks = new InstrumentsList(1, List.of(someStock));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"total\": 1," +
                "\"instruments\":[{" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"minPriceIncrement\":10," +
                "\"lot\":1," +
                "\"currency\":\"RUB\"" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getMarketStocks().get();
        assertEquals(actualResponse.getInstruments().size(), expectedStocks.getInstruments().size());
        assertEquals(actualResponse.getTotal(),expectedStocks.getTotal());
        final var stock = actualResponse.getInstruments().get(0);
        assertEquals(stock.getFigi(), someStock.getFigi());
        assertEquals(stock.getTicker(), someStock.getTicker());
        assertEquals(stock.getIsin(), someStock.getIsin());
        assertEquals(stock.getMinPriceIncrement(), someStock.getMinPriceIncrement());
        assertEquals(stock.getLot(), someStock.getLot());
        assertEquals(stock.getCurrency(), someStock.getCurrency());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/stocks"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingMarketBonds() throws ExecutionException, InterruptedException {
        final var someBond = new Instrument(
                "figi",
                "ticker",
                "isin",
                BigDecimal.TEN,
                1,
                Currency.RUB
        );
        final var expectedBonds = new InstrumentsList(1, List.of(someBond));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"total\": 1," +
                "\"instruments\":[{" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"minPriceIncrement\":10," +
                "\"lot\":1," +
                "\"currency\":\"RUB\"" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getMarketBonds().get();
        assertEquals(actualResponse.getInstruments().size(), expectedBonds.getInstruments().size());
        assertEquals(actualResponse.getTotal(),expectedBonds.getTotal());
        final var bond = actualResponse.getInstruments().get(0);
        assertEquals(bond.getFigi(), someBond.getFigi());
        assertEquals(bond.getTicker(), someBond.getTicker());
        assertEquals(bond.getIsin(), someBond.getIsin());
        assertEquals(bond.getMinPriceIncrement(), someBond.getMinPriceIncrement());
        assertEquals(bond.getLot(), someBond.getLot());
        assertEquals(bond.getCurrency(), someBond.getCurrency());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/bonds"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingMarketEtfs() throws ExecutionException, InterruptedException {
        final var someEtf = new Instrument(
                "figi",
                "ticker",
                "isin",
                BigDecimal.TEN,
                1,
                Currency.RUB
        );
        final var expectedEtfs = new InstrumentsList(1, List.of(someEtf));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"total\": 1," +
                "\"instruments\":[{" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"minPriceIncrement\":10," +
                "\"lot\":1," +
                "\"currency\":\"RUB\"" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getMarketEtfs().get();
        assertEquals(actualResponse.getInstruments().size(), expectedEtfs.getInstruments().size());
        assertEquals(actualResponse.getTotal(),expectedEtfs.getTotal());
        final var etf = actualResponse.getInstruments().get(0);
        assertEquals(etf.getFigi(), someEtf.getFigi());
        assertEquals(etf.getTicker(), someEtf.getTicker());
        assertEquals(etf.getIsin(), someEtf.getIsin());
        assertEquals(etf.getMinPriceIncrement(), someEtf.getMinPriceIncrement());
        assertEquals(etf.getLot(), someEtf.getLot());
        assertEquals(etf.getCurrency(), someEtf.getCurrency());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/etfs"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingMarketCurrencies() throws ExecutionException, InterruptedException {
        final var someCurrency = new Instrument(
                "figi",
                "ticker",
                "isin",
                BigDecimal.TEN,
                1,
                Currency.RUB
        );
        final var expectedCurrencies = new InstrumentsList(1, List.of(someCurrency));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"total\": 1," +
                "\"instruments\":[{" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"minPriceIncrement\":10," +
                "\"lot\":1," +
                "\"currency\":\"RUB\"" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.getMarketCurrencies().get();
        assertEquals(actualResponse.getInstruments().size(), expectedCurrencies.getInstruments().size());
        assertEquals(actualResponse.getTotal(),expectedCurrencies.getTotal());
        final var currency = actualResponse.getInstruments().get(0);
        assertEquals(currency.getFigi(), someCurrency.getFigi());
        assertEquals(currency.getTicker(), someCurrency.getTicker());
        assertEquals(currency.getIsin(), someCurrency.getIsin());
        assertEquals(currency.getMinPriceIncrement(), someCurrency.getMinPriceIncrement());
        assertEquals(currency.getLot(), someCurrency.getLot());
        assertEquals(currency.getCurrency(), someCurrency.getCurrency());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/currencies"))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void searchingMarketInstrumentsByTicker() throws ExecutionException, InterruptedException {
        final var someInstrument = new Instrument(
                "figi",
                "ticker",
                "isin",
                BigDecimal.TEN,
                1,
                Currency.RUB
        );
        final var expectedInstruments = new InstrumentsList(1, List.of(someInstrument));

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"total\": 1," +
                "\"instruments\":[{" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"minPriceIncrement\":10," +
                "\"lot\":1," +
                "\"currency\":\"RUB\"" +
                "}]}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.searchMarketInstrumentsByTicker(someInstrument.getTicker()).get();
        assertEquals(actualResponse.getInstruments().size(), expectedInstruments.getInstruments().size());
        assertEquals(actualResponse.getTotal(), expectedInstruments.getTotal());
        final var stock = actualResponse.getInstruments().get(0);
        assertEquals(stock.getFigi(), someInstrument.getFigi());
        assertEquals(stock.getTicker(), someInstrument.getTicker());
        assertEquals(stock.getIsin(), someInstrument.getIsin());
        assertEquals(stock.getMinPriceIncrement(), someInstrument.getMinPriceIncrement());
        assertEquals(stock.getLot(), someInstrument.getLot());
        assertEquals(stock.getCurrency(), someInstrument.getCurrency());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/search/by-ticker?ticker=" + someInstrument.getTicker()))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void searchingMarketInstrumentByFigi() throws ExecutionException, InterruptedException {
        final var expectedInstrument = new Instrument(
                "figi",
                "ticker",
                "isin",
                BigDecimal.TEN,
                1,
                Currency.RUB
        );

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"figi\":\"figi\"," +
                "\"ticker\":\"ticker\"," +
                "\"isin\":\"isin\"," +
                "\"minPriceIncrement\":10," +
                "\"lot\":1," +
                "\"currency\":\"RUB\"" +
                "}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var actualResponse = context.searchMarketInstrumentByFigi(expectedInstrument.getFigi()).get();
        assertTrue(actualResponse.isPresent());
        assertEquals(actualResponse.get().getFigi(), expectedInstrument.getFigi());
        assertEquals(actualResponse.get().getTicker(), expectedInstrument.getTicker());
        assertEquals(actualResponse.get().getIsin(), expectedInstrument.getIsin());
        assertEquals(actualResponse.get().getMinPriceIncrement(), expectedInstrument.getMinPriceIncrement());
        assertEquals(actualResponse.get().getLot(), expectedInstrument.getLot());
        assertEquals(actualResponse.get().getCurrency(), expectedInstrument.getCurrency());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/search/by-figi?figi=" + expectedInstrument.getFigi()))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void instrumentNotFound() {
        final var someTicker = "wrong_ticker";

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {" +
                "\"message\":\"Cannot find market instrument by ticker\"," +
                "\"code\":\"NOT_FOUND\"" +
                "}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(404);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        assertThrows(OpenApiException.class, () -> {
            try {
                context.searchMarketInstrumentsByTicker(someTicker).get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        });

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/market/search/by-ticker?ticker=" + someTicker))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void gettingOperations() throws ExecutionException, InterruptedException {
        final var someOperation = new Operation(
                "id",
                OperationStatus.Done,
                List.of(
                        new OperationTrade(
                                "tradeId",
                                ZonedDateTime.of(2019, 8, 30, 14, 22, 21, 66000000, ZoneId.of("UTC")),
                                BigDecimal.TEN,
                                1
                        )
                ),
                new MoneyAmount(Currency.RUB, BigDecimal.ZERO),
                Currency.RUB,
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                4,
                "figi",
                InstrumentType.Stock,
                true,
                ZonedDateTime.of(2019, 8, 19, 15, 38, 33, 131642000, ZoneId.of("UTC")),
                ExtendedOperationType.Buy
        );
        final var expectedOperations = List.of(someOperation);

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "  \"trackingId\": \"trackingId\"," +
                "  \"status\": \"Ok\"," +
                "  \"payload\": [" +
                "    {" +
                "      \"id\": \"id\"," +
                "      \"status\": \"Done\"," +
                "      \"trades\": [" +
                "        {" +
                "          \"tradeId\": \"tradeId\"," +
                "          \"date\": \"2019-08-30T14:22:21.066Z\"," +
                "          \"price\": 10," +
                "          \"quantity\": 1" +
                "        }" +
                "      ]," +
                "      \"commission\": {" +
                "        \"currency\": \"RUB\"," +
                "        \"value\": 0" +
                "      }," +
                "      \"currency\": \"RUB\"," +
                "      \"payment\": 2," +
                "      \"price\": 3," +
                "      \"quantity\": 4," +
                "      \"figi\": \"figi\"," +
                "      \"instrumentType\": \"Stock\"," +
                "      \"isMarginCall\": true," +
                "      \"date\": \"2019-08-19T18:38:33.131642+03:00\"," +
                "      \"operationType\": \"Buy\"" +
                "    }" +
                "  ]" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var from = LocalDate.of(2019, 8, 30);
        final var interval = OperationInterval.WEEK;
        final var actualResponse = context.getOperations(from, interval, someOperation.getFigi()).get();
        assertEquals(actualResponse.size(), expectedOperations.size());
        final var operation = actualResponse.get(0);
        assertEquals(operation.getId(), someOperation.getId());
        assertEquals(operation.getStatus(), someOperation.getStatus());
        assertEquals(operation.getTrades(), someOperation.getTrades());
        assertEquals(operation.getCommission(), someOperation.getCommission());
        assertEquals(operation.getCurrency(), someOperation.getCurrency());
        assertEquals(operation.getPayment(), someOperation.getPayment());
        assertEquals(operation.getPrice(), someOperation.getPrice());
        assertEquals(operation.getQuantity(), someOperation.getQuantity());
        assertEquals(operation.getFigi(), someOperation.getFigi());
        assertEquals(operation.getInstrumentType(), someOperation.getInstrumentType());
        assertEquals(operation.isMarginCall(), someOperation.isMarginCall());
        assertEquals(operation.getDate(), someOperation.getDate());
        assertEquals(operation.getOperationType(), someOperation.getOperationType());

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/operations?from=" + from.toString() + "&interval=7days&figi=" + operation.getFigi()))
                .header("Authorization", token)
                .GET()
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void subscribingToCandles() {
        final var someFigi = "figi";
        final var someInterval = CandleInterval.FIVE_MIN;
        final var request = StreamingRequest.subscribeCandle(someFigi, someInterval);

        when(webSocket.sendText(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(webSocket));

        try {
            final var result = context.sendStreamingRequest(request).get();
            assertNull(result);
        } catch (ExecutionException | InterruptedException ex) {
            fail("Future failed");
        }

        final var expectedMessage = "{\"event\":\"candle:subscribe\",\"figi\":\"figi\",\"interval\":\"5min\"}";
        verify(webSocket).sendText(expectedMessage, true);
    }

    @Test
    void unsubscribingFromCandles() {
        final var someFigi = "figi";
        final var someInterval = CandleInterval.FIVE_MIN;
        final var request = StreamingRequest.unsubscribeCandle(someFigi, someInterval);

        when(webSocket.sendText(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(webSocket));

        try {
            final var result = context.sendStreamingRequest(request).get();
            assertNull(result);
        } catch (ExecutionException | InterruptedException ex) {
            fail("Future failed");
        }

        final var expectedMessage = "{\"event\":\"candle:unsubscribe\",\"figi\":\"figi\",\"interval\":\"5min\"}";
        verify(webSocket).sendText(expectedMessage, true);
    }

    @Test
    void subscribingToOrderbook() {
        final var someFigi = "figi";
        final var someDepth = 3;
        final var request = StreamingRequest.subscribeOrderbook(someFigi, someDepth);

        when(webSocket.sendText(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(webSocket));

        try {
            final var result = context.sendStreamingRequest(request).get();
            assertNull(result);
        } catch (ExecutionException | InterruptedException ex) {
            fail("Future failed");
        }

        final var expectedMessage = "{\"event\":\"orderbook:subscribe\",\"figi\":\"figi\",\"depth\":3}";
        verify(webSocket).sendText(expectedMessage, true);
    }

    @Test
    void unsubscribingFromOrderbook() {
        final var someFigi = "figi";
        final var someDepth = 3;
        final var request = StreamingRequest.unsubscribeOrderbook(someFigi, someDepth);

        when(webSocket.sendText(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(webSocket));

        try {
            final var result = context.sendStreamingRequest(request).get();
            assertNull(result);
        } catch (ExecutionException | InterruptedException ex) {
            fail("Future failed");
        }

        final var expectedMessage = "{\"event\":\"orderbook:unsubscribe\",\"figi\":\"figi\",\"depth\":3}";
        verify(webSocket).sendText(expectedMessage, true);
    }

    @Test
    void subscribingToInstrumentInfo() {
        final var someFigi = "figi";
        final var request = StreamingRequest.subscribeInstrumentInfo(someFigi);

        when(webSocket.sendText(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(webSocket));

        try {
            final var result = context.sendStreamingRequest(request).get();
            assertNull(result);
        } catch (ExecutionException | InterruptedException ex) {
            fail("Future failed");
        }

        final var expectedMessage = "{\"event\":\"instrument_info:subscribe\",\"figi\":\"figi\"}";
        verify(webSocket).sendText(expectedMessage, true);
    }

    @Test
    void unsubscribingFromInstrumentInfo() {
        final var someFigi = "figi";
        final var request = StreamingRequest.unsubscribeInstrumentInfo(someFigi);

        when(webSocket.sendText(any(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(webSocket));

        try {
            final var result = context.sendStreamingRequest(request).get();
            assertNull(result);
        } catch (ExecutionException | InterruptedException ex) {
            fail("Future failed");
        }

        final var expectedMessage = "{\"event\":\"instrument_info:unsubscribe\",\"figi\":\"figi\"}";
        verify(webSocket).sendText(expectedMessage, true);
    }

}
