package ru.tinkoff.invest.openapi.okhttp;

import ru.tinkoff.invest.openapi.*;
import ru.tinkoff.invest.openapi.model.market.InstrumentsList;
import ru.tinkoff.invest.openapi.model.orders.Order;
import ru.tinkoff.invest.openapi.model.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class OkHttpOpenApi extends OpenApi {

    OkHttpOpenApi(MarketContext marketContext,
                  OperationsContext operationsContext,
                  OrdersContext ordersContext,
                  PortfolioContext portfolioContext,
                  StreamingContext streamingContext) {
        super(marketContext, operationsContext, ordersContext, portfolioContext, streamingContext);
    }

    public CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(final String ticker) {
        final CompletableFuture<InstrumentsList> future = new CompletableFuture<>();
        this.marketContext.searchMarketInstrumentsByTicker(ticker, (result, error) -> {
            if (Objects.isNull(error)) {
                future.complete(result);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    public CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies() {
        final CompletableFuture<PortfolioCurrencies> future = new CompletableFuture<>();
        this.portfolioContext.getPortfolioCurrencies((result, error) -> {
            if (Objects.isNull(error)) {
                future.complete(result);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    public CompletableFuture<List<Order>> getOrders() {
        final CompletableFuture<List<Order>> future = new CompletableFuture<>();
        this.ordersContext.getOrders(future::complete, future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Portfolio> getPortfolio() {
        final CompletableFuture<Portfolio> future = new CompletableFuture<>();
        this.portfolioContext.getPortfolio((result, error) -> {
            if (Objects.isNull(error)) {
                future.complete(result);
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

}
