package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.model.rest.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final class MarketContextImpl extends BaseContextImpl implements MarketContext {

    private static final String NOT_FOUND_MESSAGE_CODE = "NOT_FOUND";
    private static final String INSTRUMENT_ERROR_MESSAGE_CODE = "INSTRUMENT_ERROR";

    private static final TypeReference<MarketInstrumentListResponse> instrumentsListTypeReference =
            new TypeReference<MarketInstrumentListResponse>() {
            };
    private static final TypeReference<OrderbookResponse> orderbookTypeReference =
            new TypeReference<OrderbookResponse>() {
            };
    private static final TypeReference<CandlesResponse> historicalCandlesTypeReference =
            new TypeReference<CandlesResponse>() {
            };
    private static final TypeReference<SearchMarketInstrumentResponse> instrumentTypeReference =
            new TypeReference<SearchMarketInstrumentResponse>() {
            };

    public MarketContextImpl(@NotNull final OkHttpClient client,
                             @NotNull final String url,
                             @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @NotNull
    @Override
    public String getPath() {
        return "market";
    }

    @Override
    @NotNull
    public CompletableFuture<MarketInstrumentList> getMarketStocks() {
        final CompletableFuture<MarketInstrumentList> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("stocks")
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final MarketInstrumentListResponse result = handleResponse(response, instrumentsListTypeReference);
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
    public CompletableFuture<MarketInstrumentList> getMarketBonds() {
        final CompletableFuture<MarketInstrumentList> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("bonds")
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final MarketInstrumentListResponse result = handleResponse(response, instrumentsListTypeReference);
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
    public CompletableFuture<MarketInstrumentList> getMarketEtfs() {
        final CompletableFuture<MarketInstrumentList> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("etfs")
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final MarketInstrumentListResponse result = handleResponse(response, instrumentsListTypeReference);
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
    public CompletableFuture<MarketInstrumentList> getMarketCurrencies() {
        final CompletableFuture<MarketInstrumentList> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("currencies")
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final MarketInstrumentListResponse result = handleResponse(response, instrumentsListTypeReference);
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
    public CompletableFuture<Optional<Orderbook>> getMarketOrderbook(@NotNull final String figi, final int depth) {
        final CompletableFuture<Optional<Orderbook>> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("orderbook")
                .addQueryParameter("figi", figi)
                .addQueryParameter("depth", Integer.toString(depth))
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final OrderbookResponse result = handleResponse(response, orderbookTypeReference);
                    future.complete(Optional.of(result.getPayload()));
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(INSTRUMENT_ERROR_MESSAGE_CODE)) {
                        future.complete(Optional.empty());
                    } else {
                        future.completeExceptionally(ex);
                    }
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Optional<Candles>> getMarketCandles(@NotNull final String figi,
                                                                 @NotNull final OffsetDateTime from,
                                                                 @NotNull final OffsetDateTime to,
                                                                 @NotNull final CandleResolution interval) {
        final CompletableFuture<Optional<Candles>> future = new CompletableFuture<>();
        String renderedInterval;
        try {
            renderedInterval = mapper.writeValueAsString(interval);
            renderedInterval = renderedInterval.substring(1, renderedInterval.length() - 1);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("candles")
                .addQueryParameter("figi", figi)
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("interval", renderedInterval)
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final CandlesResponse result =
                            handleResponse(response, historicalCandlesTypeReference);
                    future.complete(Optional.of(result.getPayload()));
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(INSTRUMENT_ERROR_MESSAGE_CODE)) {
                        future.complete(Optional.empty());
                    } else {
                        future.completeExceptionally(ex);
                    }
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<MarketInstrumentList> searchMarketInstrumentsByTicker(@NotNull final String ticker) {
        final CompletableFuture<MarketInstrumentList> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-ticker")
                .addQueryParameter("ticker", ticker)
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final MarketInstrumentListResponse result = handleResponse(response, instrumentsListTypeReference);
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
    public CompletableFuture<Optional<SearchMarketInstrument>> searchMarketInstrumentByFigi(@NotNull final String figi) {
        final CompletableFuture<Optional<SearchMarketInstrument>> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-figi")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final SearchMarketInstrumentResponse result = handleResponse(response, instrumentTypeReference);
                    future.complete(Optional.of(result.getPayload()));
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(NOT_FOUND_MESSAGE_CODE)) {
                        future.complete(Optional.empty());
                    } else {
                        future.completeExceptionally(ex);
                    }
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
