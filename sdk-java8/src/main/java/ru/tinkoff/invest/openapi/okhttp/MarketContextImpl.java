package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.model.ErrorPayload;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.market.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

final class MarketContextImpl implements MarketContext {

    private static final String ACCESS_DENIED_MESSAGE_CODE = "ACCESS_DENIED";
    private static final String NOT_FOUND_MESSAGE_CODE = "NOT_FOUND";
    private static final String INSTRUMENT_ERROR_MESSAGE_CODE = "INSTRUMENT_ERROR";

    private static final TypeReference<RestResponse<ErrorPayload>> errorTypeReference =
            new TypeReference<RestResponse<ErrorPayload>>() {
            };
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

    private final String authToken;
    private final HttpUrl finalUrl;
    private final OkHttpClient client;
    private final Logger logger;
    private final ObjectMapper mapper;

    public MarketContextImpl(final OkHttpClient client,
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
        return "market";
    }

    @Override
    public void getMarketStocks(BiConsumer<InstrumentsList, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("stocks").build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);

            callback.accept(result.payload, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void getMarketBonds(BiConsumer<InstrumentsList, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("bonds").build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);

            callback.accept(result.payload, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void getMarketEtfs(BiConsumer<InstrumentsList, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("etfs").build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);

            callback.accept(result.payload, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void getMarketCurrencies(BiConsumer<InstrumentsList, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder().addPathSegment("currencies").build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);

            callback.accept(result.payload, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void getMarketOrderbook(String figi, int depth, BiConsumer<Optional<Orderbook>, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("orderbook")
                .addQueryParameter("figi", figi)
                .addQueryParameter("depth", Integer.toString(depth))
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<Orderbook> result = handleResponse(response, orderbookTypeReference);

            callback.accept(Optional.of(result.payload), null);
        } catch (OpenApiException ex) {
            if (ex.getCode().equals(INSTRUMENT_ERROR_MESSAGE_CODE)) {
                callback.accept(Optional.empty(), null);
            } else {
                callback.accept(null, ex);
            }
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void getMarketCandles(String figi,
                                 OffsetDateTime from,
                                 OffsetDateTime to,
                                 CandleInterval interval,
                                 BiConsumer<Optional<HistoricalCandles>, Throwable> callback) {
        String renderedInterval = "";
        try {
            renderedInterval = mapper.writeValueAsString(interval);
            renderedInterval = renderedInterval.substring(1, renderedInterval.length() - 1);
        } catch (JsonProcessingException ex) {
            callback.accept(null, ex);
        }

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("candles")
                .addQueryParameter("figi", figi)
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("interval", renderedInterval)
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<HistoricalCandles> result = handleResponse(response, historicalCandlesTypeReference);

            callback.accept(Optional.of(result.payload), null);
        } catch (OpenApiException ex) {
            if (ex.getCode().equals(INSTRUMENT_ERROR_MESSAGE_CODE)) {
                callback.accept(Optional.empty(), null);
            } else {
                callback.accept(null, ex);
            }
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void searchMarketInstrumentsByTicker(String ticker, BiConsumer<InstrumentsList, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-ticker")
                .addQueryParameter("ticker", ticker)
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<InstrumentsList> result = handleResponse(response, instrumentsListTypeReference);

            callback.accept(result.payload, null);
        } catch (Exception ex) {
            callback.accept(null, ex);
        }
    }

    @Override
    public void searchMarketInstrumentByFigi(String figi, BiConsumer<Optional<Instrument>, Throwable> callback) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-figi")
                .addQueryParameter("figi", figi)
                .build();
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final RestResponse<Instrument> result = handleResponse(response, instrumentTypeReference);

            callback.accept(Optional.of(result.payload), null);
        } catch (OpenApiException ex) {
            if (ex.getCode().equals(NOT_FOUND_MESSAGE_CODE)) {
                callback.accept(Optional.empty(), null);
            } else {
                callback.accept(null, ex);
            }
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
