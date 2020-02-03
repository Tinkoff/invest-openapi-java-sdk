package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.market.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MarketContextImpl extends BaseContextImpl implements MarketContext {

    private static final String NOT_FOUND_MESSAGE_CODE = "NOT_FOUND";
    private static final String INSTRUMENT_ERROR_MESSAGE_CODE = "INSTRUMENT_ERROR";

    private static final TypeReference<RestResponse<InstrumentsList>> instrumentsListTypeReference =
            new TypeReference<RestResponse<InstrumentsList>>() {
            };
    private static final TypeReference<RestResponse<Orderbook>> orderbookTypeReference =
            new TypeReference<RestResponse<Orderbook>>() {
            };
    private static final TypeReference<RestResponse<HistoricalCandles>> historicalCandlesTypeReference =
            new TypeReference<RestResponse<HistoricalCandles>>() {
            };
    private static final TypeReference<RestResponse<Instrument>> instrumentTypeReference =
            new TypeReference<RestResponse<Instrument>>() {
            };

    public MarketContextImpl(@NotNull final OkHttpClient client,
                             @NotNull final String url,
                             @NotNull final String authToken,
                             @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @NotNull
    @Override
    public String getPath() {
        return "market";
    }

    @Override
    public void getMarketStocks(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("stocks").build();
        final Request request = prepareRequest(requestUrl)
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
                    final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void getMarketBonds(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("bonds").build();
        final Request request = prepareRequest(requestUrl)
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
                    final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void getMarketEtfs(@NotNull Consumer<InstrumentsList> onComplete, @NotNull Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("etfs").build();
        final Request request = prepareRequest(requestUrl)
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
                    final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void getMarketCurrencies(@NotNull Consumer<InstrumentsList> onComplete,
                                    @NotNull Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("currencies").build();
        final Request request = prepareRequest(requestUrl)
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
                    final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void getMarketOrderbook(@NotNull String figi,
                                   int depth,
                                   @NotNull Consumer<Optional<Orderbook>> onComplete,
                                   @NotNull Consumer<Throwable> onError) {
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
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final RestResponse<Orderbook> result = handleResponse(response, orderbookTypeReference);
                    onComplete.accept(Optional.of(result.payload));
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(INSTRUMENT_ERROR_MESSAGE_CODE)) {
                        onComplete.accept(Optional.empty());
                    } else {
                        onError.accept(ex);
                    }
                }
            }
        });
    }

    @Override
    public void getMarketCandles(@NotNull String figi,
                                 @NotNull OffsetDateTime from,
                                 @NotNull OffsetDateTime to,
                                 @NotNull CandleInterval interval,
                                 @NotNull Consumer<Optional<HistoricalCandles>> onComplete,
                                 @NotNull Consumer<Throwable> onError) {
        String renderedInterval;
        try {
            renderedInterval = mapper.writeValueAsString(interval);
            renderedInterval = renderedInterval.substring(1, renderedInterval.length() - 1);
        } catch (JsonProcessingException ex) {
            onError.accept(ex);
            return;
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
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final RestResponse<HistoricalCandles> result =
                            handleResponse(response, historicalCandlesTypeReference);
                    onComplete.accept(Optional.of(result.payload));
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(INSTRUMENT_ERROR_MESSAGE_CODE)) {
                        onComplete.accept(Optional.empty());
                    } else {
                        onError.accept(ex);
                    }
                }
            }
        });
    }

    @Override
    public void searchMarketInstrumentsByTicker(@NotNull String ticker,
                                                @NotNull Consumer<InstrumentsList> onComplete,
                                                @NotNull Consumer<Throwable> onError) {
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
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

    @Override
    public void searchMarketInstrumentByFigi(@NotNull String figi,
                                             @NotNull Consumer<Optional<Instrument>> onComplete,
                                             @NotNull Consumer<Throwable> onError) {
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
                logger.log(Level.SEVERE, "При запросе к REST API произошла ошибка", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final RestResponse<Instrument> result = handleResponse(response, instrumentTypeReference);
                    onComplete.accept(Optional.of(result.payload));
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(NOT_FOUND_MESSAGE_CODE)) {
                        onComplete.accept(Optional.empty());
                    } else {
                        onError.accept(ex);
                    }
                }
            }
        });
    }

}
