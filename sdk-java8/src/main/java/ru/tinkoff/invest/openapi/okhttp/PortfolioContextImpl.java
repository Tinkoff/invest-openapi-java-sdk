package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.model.rest.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

final class PortfolioContextImpl extends BaseContextImpl implements PortfolioContext {

    private static final TypeReference<PortfolioResponse> portfolioTypeReference =
            new TypeReference<PortfolioResponse>() {
            };
    private static final TypeReference<PortfolioCurrenciesResponse> portfolioCurrenciesTypeReference =
            new TypeReference<PortfolioCurrenciesResponse>() {
            };

    public PortfolioContextImpl(@NotNull final OkHttpClient client,
                                @NotNull final String url,
                                @NotNull final String authToken) {
        super(client, url, authToken);
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
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                try {
                    final PortfolioResponse result = handleResponse(response, portfolioTypeReference);
                    future.complete(result.getPayload());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Currencies> getPortfolioCurrencies(@Nullable final String brokerAccountId) {
        final CompletableFuture<Currencies> future = new CompletableFuture<>();

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
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                try {
                    final PortfolioCurrenciesResponse result = handleResponse(response, portfolioCurrenciesTypeReference);
                    future.complete(result.getPayload());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
