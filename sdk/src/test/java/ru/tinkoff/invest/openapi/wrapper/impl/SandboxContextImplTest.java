package ru.tinkoff.invest.openapi.wrapper.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.SandboxContext;
import ru.tinkoff.invest.openapi.wrapper.WebSocketListener;
import ru.tinkoff.invest.openapi.data.Currency;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

class SandboxContextImplTest {

    private final static String host = "http://localhost/openapi-gw";
    private final static String token = "super_token";
    private static SandboxContext context;
    private static HttpClient httpClient;

    private abstract static class HttpStringResponse implements HttpResponse<String> {}

    @BeforeAll
    static void initTest() {
        final var logger = Logger.getLogger(ContextImplTest.class.getName());

        httpClient = mock(HttpClient.class);
        final WebSocketListener listener = mock(WebSocketListener.class);
        final Connection connection = mock(ConnectionImpl.class);
        when(connection.getHost()).thenReturn(host);
        when(connection.getAuthToken()).thenReturn(token);
        when(connection.getHttpClient()).thenReturn(httpClient);
        when(connection.getListener()).thenReturn(listener);

        context = new SandboxContextImpl(connection, logger);
    }

    @Test
    void performingRegistration() throws ExecutionException, InterruptedException {
        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var result = context.performRegistration().get();
        assertNull(result);

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/sandbox/register"))
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void settingCurrencyBalance() throws ExecutionException, InterruptedException {
        final var someCurrency = Currency.RUB;
        final var someBalance = BigDecimal.valueOf(1000);

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var result = context.setCurrencyBalance(someCurrency, someBalance).get();
        assertNull(result);

        final var requestBody = "{\"currency\":\"RUB\",\"balance\":1000}";
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/sandbox/currencies/balance"))
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void settingPositionBalance() throws ExecutionException, InterruptedException {
        final var someFigi = "figi";
        final var someBalance = BigDecimal.valueOf(1000);

        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var result = context.setPositionBalance(someFigi, someBalance).get();
        assertNull(result);

        final var requestBody = "{\"figi\":\"figi\",\"balance\":1000}";
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/sandbox/positions/balance"))
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void clearAll() throws ExecutionException, InterruptedException {
        final HttpResponse<String> response = mock(HttpStringResponse.class);
        final String json = "{" +
                "\"trackingId\":\"trackingId\"," +
                "\"status\":\"Ok\"," +
                "\"payload\": {}" +
                "}";
        when(response.body()).thenReturn(json);
        when(response.statusCode()).thenReturn(200);

        when(httpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        final var result = context.clearAll().get();
        assertNull(result);

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(host + "/sandbox/clear"))
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        verify(httpClient).sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

}
