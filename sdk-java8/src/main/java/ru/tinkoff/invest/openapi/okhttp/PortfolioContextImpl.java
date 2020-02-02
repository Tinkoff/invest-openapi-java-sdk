package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.model.ErrorPayload;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.orders.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class PortfolioContextImpl extends BaseContextImpl implements PortfolioContext {

    private static final TypeReference<RestResponse<Portfolio>> portfolioTypeReference =
            new TypeReference<RestResponse<Portfolio>>() {
            };
    private static final TypeReference<RestResponse<PortfolioCurrencies>> portfolioCurrenciesTypeReference =
            new TypeReference<RestResponse<PortfolioCurrencies>>() {
            };

    public PortfolioContextImpl(@NotNull final OkHttpClient client,
                                @NotNull final String url,
                                @NotNull final String authToken,
                                @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @NotNull
    @Override
    public String getPath() {
        return "portfolio";
    }

    @Override
    public void getPortfolio(@NotNull final Consumer<Portfolio> onComplete,
                             @NotNull final Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().build();
        final Request request = prepareRequest(requestUrl)
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
                    final RestResponse<Portfolio> result = handleResponse(response, portfolioTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(null);
                }
            }
        });
    }

    @Override
    public void getPortfolioCurrencies(@NotNull final Consumer<PortfolioCurrencies> onComplete,
                                       @NotNull final Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("currencies").build();
        final Request request = prepareRequest(requestUrl)
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
                    final RestResponse<PortfolioCurrencies> result = handleResponse(response, portfolioCurrenciesTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

}
