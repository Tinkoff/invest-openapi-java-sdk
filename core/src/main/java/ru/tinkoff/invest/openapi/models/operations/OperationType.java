package ru.tinkoff.invest.openapi.models.operations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Перечислние возможных типов операции.
 */
public enum OperationType {
    @JsonProperty("Buy")
    Buy,
    @JsonProperty("BuyCard")
    BuyCard,
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
    @JsonProperty("OtherCommission")
    OtherCommission,
    @JsonProperty("PayIn")
    PayIn,
    @JsonProperty("PayOut")
    PayOut,
    @JsonProperty("Tax")
    Tax,
    @JsonProperty("TaxLucre")
    TaxLucre,
    @JsonProperty("TaxDividend")
    TaxDividend,
    @JsonProperty("TaxCoupon")
    TaxCoupon,
    @JsonProperty("TaxBack")
    TaxBack,
    @JsonProperty("Repayment")
    Repayment,
    @JsonProperty("PartRepayment")
    PartRepayment,
    @JsonProperty("Coupon")
    Coupon,
    @JsonProperty("Dividend")
    Dividend,
    @JsonProperty("SecurityIn")
    SecurityIn,
    @JsonProperty("SecurityOut")
    SecurityOut
}
