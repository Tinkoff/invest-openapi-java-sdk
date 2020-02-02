package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.model.RestResponse;
import ru.tinkoff.invest.openapi.model.operations.OperationsList;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
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
    public void getOperations(@NotNull final OffsetDateTime from,
                              @NotNull final OffsetDateTime to,
                              @NotNull final String figi,
                              @NotNull final Consumer<OperationsList> onComplete,
                              @NotNull final Consumer<Throwable> onError) {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addQueryParameter("figi", figi)
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .build();
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
                    final RestResponse<OperationsList> result = handleResponse(response, operationsListTypeReference);
                    onComplete.accept(result.payload);
                } catch (OpenApiException ex) {
                    onError.accept(ex);
                }
            }
        });
    }

}
