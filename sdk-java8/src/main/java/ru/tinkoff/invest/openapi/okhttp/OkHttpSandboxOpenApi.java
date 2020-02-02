package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;
import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;

import java.util.concurrent.CompletableFuture;

public class OkHttpSandboxOpenApi extends OkHttpOpenApi {

    public final SandboxContext sandboxContext;

    OkHttpSandboxOpenApi(@NotNull final MarketContext marketContext,
                         @NotNull final OperationsContext operationsContext,
                         @NotNull final OrdersContext ordersContext,
                         @NotNull final PortfolioContext portfolioContext,
                         @NotNull final StreamingContext streamingContext,
                         @NotNull final SandboxContext sandboxContext) {
        super(marketContext, operationsContext, ordersContext, portfolioContext, streamingContext);
        this.sandboxContext = sandboxContext;
    }

    @NotNull
    public CompletableFuture<Void> performRegistration() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.sandboxContext.performRegistration(future::complete, future::completeExceptionally);
        return future;
    }

    @NotNull
    public CompletableFuture<Void> clearAll() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.sandboxContext.clearAll(future::complete, future::completeExceptionally);
        return future;
    }

    @NotNull
    public CompletableFuture<Void> setCurrencyBalance(CurrencyBalance data) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.sandboxContext.setCurrencyBalance(data, future::complete, future::completeExceptionally);
        return future;
    }

}
