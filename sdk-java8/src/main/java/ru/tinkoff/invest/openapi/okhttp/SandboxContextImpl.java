package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.model.EmptyPayload;
import ru.tinkoff.invest.openapi.model.ErrorPayload;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.model.sandbox.PositionBalance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

final class SandboxContextImpl implements SandboxContext {

    private static final String ACCESS_DENIED_MESSAGE_CODE = "ACCESS_DENIED";

    private static final TypeReference<RestResponse<ErrorPayload>> errorTypeReference =
            new TypeReference<RestResponse<ErrorPayload>>() {
            };
    private static final TypeReference<RestResponse<EmptyPayload>> emptyPayloadTypeReference =
            new TypeReference<RestResponse<EmptyPayload>>() {
            };

    private final String authToken;
    private final HttpUrl finalUrl;
    private final OkHttpClient client;
    private final Logger logger;
    private final ObjectMapper mapper;

    public SandboxContextImpl(OkHttpClient client, String url, String authToken, Logger logger) {
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
        return "sandbox";
    }

    @Override
    public void performRegistration(BiConsumer<Void, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("register")
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .post(RequestBody.create(new byte[] {}))
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response, emptyPayloadTypeReference);

            callback.accept(null, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void setCurrencyBalance(CurrencyBalance data, BiConsumer<Void, Throwable> callback) {
        String renderedBody = "";
        try {
            renderedBody = mapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            callback.accept(null, ex);
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("currencies")
                .addPathSegment("balance")
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response, emptyPayloadTypeReference);

            callback.accept(null, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void setPositionBalance(PositionBalance data, BiConsumer<Void, Throwable> callback) {
        String renderedBody = "";
        try {
            renderedBody = mapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            callback.accept(null, ex);
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("positions")
                .addPathSegment("balance")
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response, emptyPayloadTypeReference);

            callback.accept(null, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void clearAll(BiConsumer<Void, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("clear")
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .post(RequestBody.create(new byte[] {}))
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response, emptyPayloadTypeReference);

            callback.accept(null, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
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
