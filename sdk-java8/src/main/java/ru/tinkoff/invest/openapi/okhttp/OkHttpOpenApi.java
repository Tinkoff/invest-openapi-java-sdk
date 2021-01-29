package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.*;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public final class OkHttpOpenApi extends OpenApi {

    private final Executor executor;
    private final OkHttpClient client;
    private final String apiUrl;

    private SandboxContext sandboxContext;
    private OrdersContext ordersContext;
    private PortfolioContext portfolioContext;
    private MarketContext marketContext;
    private OperationsContext operationsContext;
    private UserContext userContext;
    private StreamingContext streamingContext;

    public OkHttpOpenApi(@NotNull final String token,
                         final boolean sandboxMode,
                         @NotNull final Executor executor) {
        super(token, sandboxMode);
        this.executor = executor;
        this.client = new OkHttpClient.Builder()
                .pingInterval(Duration.ofSeconds(5))
                .build();
        this.apiUrl = sandboxMode ? this.config.sandboxApiUrl : this.config.marketApiUrl;
    }

    public OkHttpOpenApi(@NotNull final String token, final boolean sandboxMode) {
        this(token, sandboxMode, ForkJoinPool.commonPool());
    }

    @Override
    public void close() {
        this.client.dispatcher().executorService().shutdown();
    }

    @NotNull
    public SandboxContext getSandboxContext() {
        if (this.isSandboxMode) {
            if (this.sandboxContext == null) {
                this.sandboxContext = new SandboxContextImpl(client, apiUrl, authToken);
            }
            return this.sandboxContext;
        } else {
            throw new RuntimeException("Попытка воспользоваться \"песочным\" контекстом API не в режиме \"песочницы\"");
        }
    }

    @NotNull
    public OrdersContext getOrdersContext() {
        if (this.ordersContext == null) {
            this.ordersContext = new OrdersContextImpl(client, apiUrl, authToken);
        }
        return this.ordersContext;
    }

    @NotNull
    public PortfolioContext getPortfolioContext() {
        if (Objects.isNull(this.portfolioContext)) {
            this.portfolioContext = new PortfolioContextImpl(client, apiUrl, authToken);
        }
        return this.portfolioContext;
    }

    @NotNull
    public MarketContext getMarketContext() {
        if (this.marketContext == null) {
            this.marketContext = new MarketContextImpl(client, apiUrl, authToken);
        }
        return this.marketContext;
    }

    @NotNull
    public OperationsContext getOperationsContext() {
        if (this.operationsContext == null) {
            this.operationsContext = new OperationsContextImpl(client, apiUrl, authToken);
        }
        return this.operationsContext;
    }

    @NotNull
    public UserContext getUserContext() {
        if (this.userContext == null) {
            this.userContext = new UserContextImpl(client, apiUrl, authToken);
        }
        return this.userContext;
    }

    @NotNull
    public StreamingContext getStreamingContext() {
        if (this.streamingContext == null) {
            this.streamingContext = new StreamingContextImpl(
                    client,
                    this.config.streamingUrl,
                    authToken,
                    this.config.streamingParallelism,
                    executor
            );
        }
        return this.streamingContext;
    }

}
