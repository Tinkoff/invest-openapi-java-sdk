package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;

public class OkHttpSandboxOpenApi extends OkHttpOpenApi {

    @NotNull
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

}
