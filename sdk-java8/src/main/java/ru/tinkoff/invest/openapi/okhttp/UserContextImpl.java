package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.UserContext;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.models.RestResponse;
import ru.tinkoff.invest.openapi.models.user.AccountsList;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

final class UserContextImpl extends BaseContextImpl implements UserContext {

    private static final TypeReference<RestResponse<AccountsList>> accountsListTypeReference =
            new TypeReference<RestResponse<AccountsList>>() {
            };

    public UserContextImpl(@NotNull final OkHttpClient client,
                           @NotNull final String url,
                           @NotNull final String authToken,
                           @NotNull final Logger logger) {
        super(client, url, authToken, logger);
    }

    @NotNull
    @Override
    public String getPath() {
        return "user";
    }

    @Override
    @NotNull
    public CompletableFuture<AccountsList> getAccounts() {
        final CompletableFuture<AccountsList> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("accounts")
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
                    final RestResponse<AccountsList> result = handleResponse(response, accountsListTypeReference);
                    future.complete(result.payload);
                } catch (OpenApiException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
