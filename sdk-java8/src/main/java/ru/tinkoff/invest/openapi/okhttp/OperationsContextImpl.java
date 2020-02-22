package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.models.RestResponse;
import ru.tinkoff.invest.openapi.models.operations.OperationsList;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class OperationsContextImpl extends BaseContextImpl implements OperationsContext {

    private static final TypeReference<RestResponse<OperationsList>> operationsListTypeReference =
            new TypeReference<RestResponse<OperationsList>>() {
            };

    public OperationsContextImpl(@NotNull final OkHttpClient client,
                                 @NotNull final String url,
                                 @NotNull final String authToken,
                                 @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @NotNull
    @Override
    public String getPath() {
        return "operations";
    }

    @Override
    @NotNull
    public CompletableFuture<OperationsList> getOperations(@NotNull final OffsetDateTime from,
                                                           @NotNull final OffsetDateTime to,
                                                           @Nullable final String figi,
                                                           @Nullable final String brokerAccountId) {
        final CompletableFuture<OperationsList> future = new CompletableFuture<>();
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(figi) && !figi.isEmpty())
            builder.addQueryParameter("figi", figi);
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
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
                    final RestResponse<OperationsList> result = handleResponse(response, operationsListTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
