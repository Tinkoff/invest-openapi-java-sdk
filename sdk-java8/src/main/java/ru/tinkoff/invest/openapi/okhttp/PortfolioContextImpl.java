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
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.model.ErrorPayload;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

final class PortfolioContextImpl implements PortfolioContext {

    private static final String ACCESS_DENIED_MESSAGE_CODE = "ACCESS_DENIED";

    private static final TypeReference<RestResponse<ErrorPayload>> errorTypeReference =
            new TypeReference<RestResponse<ErrorPayload>>() {
            };
    private static final TypeReference<RestResponse<Portfolio>> portfolioTypeReference =
            new TypeReference<RestResponse<Portfolio>>() {
            };
    private static final TypeReference<RestResponse<PortfolioCurrencies>> portfolioCurrenciesTypeReference =
            new TypeReference<RestResponse<PortfolioCurrencies>>() {
            };

    private final String authToken;
    private final HttpUrl finalUrl;
    private final OkHttpClient client;
    private final Logger logger;
    private final ObjectMapper mapper;

    public PortfolioContextImpl(final OkHttpClient client,
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
        return "portfolio";
    }

    @Override
    public void getPortfolio(BiConsumer<Portfolio, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder().build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<Portfolio> result = handleResponse(response, portfolioTypeReference);

            callback.accept(result.payload, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void getPortfolioCurrencies(BiConsumer<PortfolioCurrencies, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("currencies").build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<PortfolioCurrencies> result = handleResponse(response, portfolioCurrenciesTypeReference);

            callback.accept(result.payload, null);
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
