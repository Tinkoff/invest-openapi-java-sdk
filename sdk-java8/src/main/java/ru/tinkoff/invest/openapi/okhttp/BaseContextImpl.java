package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.Context;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.exceptions.WrongTokenException;
import ru.tinkoff.invest.openapi.models.EmptyPayload;
import ru.tinkoff.invest.openapi.models.ErrorPayload;
import ru.tinkoff.invest.openapi.models.RestResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class BaseContextImpl implements Context {

    protected static final TypeReference<RestResponse<ErrorPayload>> errorTypeReference =
            new TypeReference<RestResponse<ErrorPayload>>() {
            };
    protected static final TypeReference<RestResponse<EmptyPayload>> emptyPayloadTypeReference =
            new TypeReference<RestResponse<EmptyPayload>>() {
            };

    protected final String authToken;
    protected final HttpUrl finalUrl;
    protected final OkHttpClient client;
    protected final ObjectMapper mapper;
    protected final Logger logger;

    public BaseContextImpl(@NotNull final OkHttpClient client,
                           @NotNull final String url,
                           @NotNull final String authToken,
                           @NotNull final Logger logger) {

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

    @NotNull
    protected Request.Builder prepareRequest(@NotNull final HttpUrl requestUrl) {
        return new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken);
    }

    @NotNull
    protected <D> D handleResponse(@NotNull final Response response,
                                   @NotNull final TypeReference<D> tr) throws IOException, OpenApiException {
        switch (response.code()) {
            case 200:
                final InputStream bodyStream = Objects.requireNonNull(response.body()).byteStream();
                return mapper.readValue(bodyStream, tr);
            case 401:
                throw new WrongTokenException();
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
