package ru.tinkoff.invest.openapi.models.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Модель списка операций возвращаемая OpenAPI.
 */
public final class AccountsList {

    /**
     * Непосредственно список операций.
     */
    @NotNull
    public final List<BrokerAccount> accounts;

    @JsonCreator
    public AccountsList(@JsonProperty(value = "accounts", required = true)
                        @NotNull
                        final List<BrokerAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccountsList(");
        sb.append("accounts=").append(accounts);
        sb.append(')');
        return sb.toString();
    }
}
