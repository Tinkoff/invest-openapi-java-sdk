package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;

public class OkHttpOpenApi extends OpenApi {

    OkHttpOpenApi(@NotNull final MarketContext marketContext,
                  @NotNull final OperationsContext operationsContext,
                  @NotNull final OrdersContext ordersContext,
                  @NotNull final PortfolioContext portfolioContext,
                  @NotNull final UserContext userContext,
                  @NotNull final StreamingContext streamingContext) {
        super(marketContext, operationsContext, ordersContext, portfolioContext, userContext, streamingContext);
    }

}
