package ru.tinkoff.invest.openapi.models.operations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечислние возможных типов операции.
 */
public enum OperationType {
    @JsonProperty("Buy")
    Buy,
    @JsonProperty("Sell")
    Sell,
    @JsonProperty("BrokerCommission")
    BrokerCommission,
    @JsonProperty("ExchangeCommission")
    ExchangeCommission,
    @JsonProperty("ServiceCommission")
    ServiceCommission,
    @JsonProperty("MarginCommission")
    MarginCommission,
    @JsonProperty("BuyCard")
    BuyCard,
    @JsonProperty("Dividend")
    Dividend,
    @JsonProperty("TaxDividend")
    TaxDividend,
    @JsonProperty("TaxCoupon")
    TaxCoupon,
    @JsonProperty("Coupon")
    Coupon,
    @JsonProperty("PartRepayment")
    PartRepayment,
    @JsonProperty("PayIn")
    PayIn
}
