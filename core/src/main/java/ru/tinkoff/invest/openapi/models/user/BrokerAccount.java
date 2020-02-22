package ru.tinkoff.invest.openapi.models.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 * Операция с инструментом производённая брокером.
 */
public final class BrokerAccount {

    /**
     * Тип операции.
     */
    @NotNull
    public final BrokerAccountType brokerAccountType;

    /**
     * Идентификатор операции.
     */
    @NotNull
    public final String brokerAccountId;

    @JsonCreator
    public BrokerAccount(@JsonProperty(value = "brokerAccountType", required = true)
                         @NotNull
                         final BrokerAccountType brokerAccountType,
                         @JsonProperty(value = "brokerAccountId", required = true)
                         @NotNull
                         final String brokerAccountId) {
        this.brokerAccountType = brokerAccountType;
        this.brokerAccountId = brokerAccountId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BrokerAccount(");
        sb.append("brokerAccountType=").append(brokerAccountType);
        sb.append(", brokerAccountId='").append(brokerAccountId).append('\'');
        sb.append(')');
        return sb.toString();
    }

}
