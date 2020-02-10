package ru.tinkoff.invest.openapi.models.market;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Возможные торговые статусы.
 */
public enum TradeStatus {
    @JsonProperty("NormalTrading")
    NormalTrading,
    @JsonProperty("NotAvailableForTrading")
    NotAvailableForTrading
}
