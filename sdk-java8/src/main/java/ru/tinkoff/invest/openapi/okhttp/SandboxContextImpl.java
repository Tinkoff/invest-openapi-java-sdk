package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.model.sandbox.PositionBalance;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SandboxContextImpl extends BaseContextImpl implements SandboxContext {

    public SandboxContextImpl(@NotNull final OkHttpClient client,
                              @NotNull final String url,
                              @NotNull final String authToken,
                              @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @NotNull
    @Override
    public String getPath() {
        return "sandbox";
    }

    @Override
    public void performRegistration(@NotNull final Consumer<Void> onComplete,
                                    @NotNull final Consumer<Throwable> onError) {
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
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    onComplete.accept(null);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void setCurrencyBalance(@NotNull final CurrencyBalance data,
                                   @NotNull final Consumer<Void> onComplete,
                                   @NotNull final Consumer<Throwable> onError) {
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            onError.accept(ex);
            return;
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
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    onComplete.accept(null);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void setPositionBalance(@NotNull final PositionBalance data,
                                   @NotNull final Consumer<Void> onComplete,
                                   @NotNull final Consumer<Throwable> onError) {
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            onError.accept(ex);
            return;
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
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    onComplete.accept(null);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void clearAll(@NotNull final Consumer<Void> onComplete,
                         @NotNull final Consumer<Throwable> onError) {
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
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    onComplete.accept(null);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

}
