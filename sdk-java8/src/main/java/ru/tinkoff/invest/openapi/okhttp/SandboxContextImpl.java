package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.models.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.models.sandbox.PositionBalance;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SandboxContextImpl extends BaseContextImpl implements SandboxContext {

    public SandboxContextImpl(@NotNull final OkHttpClient client,
                              @NotNull final String url,
                              @NotNull final String authToken,
                              @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @Override
    @NotNull
    public String getPath() {
        return "sandbox";
    }

    @Override
    @NotNull
    public CompletableFuture<Void> performRegistration() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("register")
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[] {}))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> setCurrencyBalance(@NotNull final CurrencyBalance data) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("currencies")
                .addPathSegment("balance")
                .build();
        Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> setPositionBalance(@NotNull final PositionBalance data) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("positions")
                .addPathSegment("balance")
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> clearAll() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("clear")
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[] {}))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
