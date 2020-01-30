package ru.tinkoff.invest.openapi.okhttp;

import ru.tinkoff.invest.openapi.*;
import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class OkHttpSandboxOpenApi extends OkHttpOpenApi {

    public final SandboxContext sandboxContext;

    OkHttpSandboxOpenApi(MarketContext marketContext,
                         OperationsContext operationsContext,
                         OrdersContext ordersContext,
                         PortfolioContext portfolioContext,
                         StreamingContext streamingContext,
                         SandboxContext sandboxContext) {
        super(marketContext, operationsContext, ordersContext, portfolioContext, streamingContext);
        this.sandboxContext = sandboxContext;
    }

    public CompletableFuture<Void> performRegistration() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.sandboxContext.performRegistration((result, error) -> {
            if (Objects.isNull(error)) {
                future.complete(null);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    public CompletableFuture<Void> clearAll() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.sandboxContext.clearAll((result, error) -> {
            if (Objects.isNull(error)) {
                future.complete(null);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    public CompletableFuture<Void> setCurrencyBalance(CurrencyBalance data) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.sandboxContext.setCurrencyBalance(data, (result, error) -> {
            if (Objects.isNull(error)) {
                future.complete(null);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

}
