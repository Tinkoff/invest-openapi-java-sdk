package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;
import ru.tinkoff.invest.openapi.model.market.InstrumentsList;
import ru.tinkoff.invest.openapi.model.orders.Order;
import ru.tinkoff.invest.openapi.model.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class OkHttpOpenApi extends OpenApi {

    OkHttpOpenApi(@NotNull final MarketContext marketContext,
                  @NotNull final OperationsContext operationsContext,
                  @NotNull final OrdersContext ordersContext,
                  @NotNull final PortfolioContext portfolioContext,
                  @NotNull final StreamingContext streamingContext) {
        super(marketContext, operationsContext, ordersContext, portfolioContext, streamingContext);
    }

    @NotNull
    public CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(@NotNull final String ticker) {
        final CompletableFuture<InstrumentsList> future = new CompletableFuture<>();
        this.marketContext.searchMarketInstrumentsByTicker(ticker, future::complete, future::completeExceptionally);
        return future;
    }

    @NotNull
    public CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies() {
        final CompletableFuture<PortfolioCurrencies> future = new CompletableFuture<>();
        this.portfolioContext.getPortfolioCurrencies(future::complete, future::completeExceptionally);
        return future;
    }

    @NotNull
    public CompletableFuture<List<Order>> getOrders() {
        final CompletableFuture<List<Order>> future = new CompletableFuture<>();
        this.ordersContext.getOrders(future::complete, future::completeExceptionally);
        return future;
    }

    @NotNull
    public CompletableFuture<Portfolio> getPortfolio() {
        final CompletableFuture<Portfolio> future = new CompletableFuture<>();
        this.portfolioContext.getPortfolio(future::complete, future::completeExceptionally);
        return future;
    }

}
