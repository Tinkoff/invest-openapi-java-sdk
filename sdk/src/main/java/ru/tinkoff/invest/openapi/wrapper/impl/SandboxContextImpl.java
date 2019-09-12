package ru.tinkoff.invest.openapi.wrapper.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.SandboxContext;
import ru.tinkoff.invest.openapi.data.Currency;
import ru.tinkoff.invest.openapi.data.OpenApiResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

class SandboxContextImpl extends ContextImpl implements SandboxContext {

    private static final String REGISTER_PATH = "/sandbox/register";
    private static final String CURRENCIES_BALANCE_PATH = "/sandbox/currencies/balance";
    private static final String POSITIONS_BALANCE_PATH = "/sandbox/positions/balance";
    private static final String CLEAR_PATH = "/sandbox/clear";

    private static class CurrencyBalanceDto {
        public final Currency currency;
        public final BigDecimal balance;

        CurrencyBalanceDto(Currency currency, BigDecimal balance) {
            this.currency = currency;
            this.balance = balance;
        }
    }

    private static class PositionBalanceDto {
        public final String figi;
        public final BigDecimal balance;

        PositionBalanceDto(String figi, BigDecimal balance) {
            this.figi = figi;
            this.balance = balance;
        }
    }

    SandboxContextImpl(Connection connection, Logger logger) {
        super(connection, logger);
    }

    @Override
    public CompletableFuture<Void> performRegistration() {
        return sendPostRequest(REGISTER_PATH, null, new TypeReference<OpenApiResponse<EmptyPayload>>(){})
                .thenApply(ep -> null);
    }

    @Override
    public CompletableFuture<Void> setCurrencyBalance(Currency currency, BigDecimal balance) {
        var payload = new CurrencyBalanceDto(currency, balance);
        return sendPostRequest(CURRENCIES_BALANCE_PATH, payload, new TypeReference<OpenApiResponse<EmptyPayload>>(){})
                .thenApply(ep -> null);
    }

    @Override
    public CompletableFuture<Void> setPositionBalance(String figi, BigDecimal balance) {
        var payload = new PositionBalanceDto(figi, balance);
        return sendPostRequest(POSITIONS_BALANCE_PATH, payload, new TypeReference<OpenApiResponse<EmptyPayload>>(){})
                .thenApply(ep -> null);
    }

    @Override
    public CompletableFuture<Void> clearAll() {
        return sendPostRequest(CLEAR_PATH, null, new TypeReference<OpenApiResponse<EmptyPayload>>(){})
                .thenApply(ep -> null);
    }
}
