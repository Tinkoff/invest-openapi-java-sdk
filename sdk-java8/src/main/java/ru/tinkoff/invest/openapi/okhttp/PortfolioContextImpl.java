package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.models.RestResponse;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    @NotNull
    public CompletableFuture<Portfolio> getPortfolio(@Nullable final String brokerAccountId) {
        final CompletableFuture<Portfolio> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    final RestResponse<Portfolio> result = handleResponse(response, portfolioTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies(@Nullable final String brokerAccountId) {
        final CompletableFuture<PortfolioCurrencies> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("currencies")
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) throws IOException {
                try {
                    final RestResponse<PortfolioCurrencies> result = handleResponse(response, portfolioCurrenciesTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
