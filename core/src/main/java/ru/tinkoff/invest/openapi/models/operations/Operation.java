package ru.tinkoff.invest.openapi.models.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Операция с инструментом производённая брокером.
 */
public final class Operation {

    /**
     * Идентификатор операции.
     */
    @NotNull
    public final String id;

    /**
     * Статус операции.
     */
    @NotNull
    public final OperationStatus status;

    /**
     * Сделки, произведённые в рамках операции.
     */
    @Nullable
    public final List<Trade> trades;

    /**
     * Размер коммиссии.
     */
    @Nullable
    public final MoneyAmount commission;

    /**
     * Валюта, в которой производится операция.
     */
    @NotNull
    public final Currency currency;

    /**
     * Полная сумма операции.
     */
    @NotNull
    public final BigDecimal payment;

    /**
     * Цена за единицу.
     */
    @Nullable
    public final BigDecimal price;

    /**
     * Объём операции.
     */
    @Nullable
    public final Integer quantity;

    /**
     * Идентификатор инструмента.
     */
    @Nullable
    public final String figi;

    /**
     * Тип инструмента.
     */
    @Nullable
    public final InstrumentType instrumentType;

    /**
     * Флаг маржинальной торговли.
     */
    public final boolean isMarginCall;

    /**
     * Дата/время исполнения операции.
     */
    @NotNull
    public final OffsetDateTime date;

    /**
     * Тип операции.
     */
    @Nullable
    public final OperationType operationType;

    @JsonCreator
    public Operation(@JsonProperty(value = "id", required = true)
                     @NotNull
                     final String id,
                     @JsonProperty(value = "status", required = true)
                     @NotNull
                     final OperationStatus status,
                     @JsonProperty("trades")
                     @Nullable
                     final List<Trade> trades,
                     @JsonProperty("commission")
                     @Nullable
                     final MoneyAmount commission,
                     @JsonProperty(value = "currency", required = true)
                     @NotNull
                     final Currency currency,
                     @JsonProperty(value = "payment", required = true)
                     @NotNull
                     final BigDecimal payment,
                     @JsonProperty("price")
                     @Nullable
                     final BigDecimal price,
                     @JsonProperty("quantity")
                     @Nullable
                     final Integer quantity,
                     @JsonProperty("figi")
                     @Nullable
                     final String figi,
                     @JsonProperty("instrumentType")
                     @Nullable
                     final InstrumentType instrumentType,
                     @JsonProperty(value = "isMarginCall", required = true)
                     final boolean isMarginCall,
                     @JsonProperty(value = "date", required = true)
                     @NotNull
                     final OffsetDateTime date,
                     @JsonProperty("operationType")
                     @Nullable
                     final OperationType operationType) {
        this.id = id;
        this.status = status;
        this.trades = trades;
        this.commission = commission;
        this.currency = currency;
        this.payment = payment;
        this.price = price;
        this.quantity = quantity;
        this.figi = figi;
        this.instrumentType = instrumentType;
        this.isMarginCall = isMarginCall;
        this.date = date;
        this.operationType = operationType;
    }

    /**
     * Модель сделки в рамках операции.
     */
    public static class Trade {

        /**
         * Идентификатор сделки.
         */
        @NotNull
        public final String tradeId;

        /**
         * Дата/время совершения сделки.
         */
        @NotNull
        public final OffsetDateTime date;

        /**
         * Цена за единицу.
         */
        @NotNull
        public final BigDecimal price;

        /**
         * Объём сделки.
         */
        public final int quantity;

        @JsonCreator
        public Trade(@JsonProperty(value = "tradeId", required = true)
                     @NotNull
                     final String tradeId,
                     @JsonProperty(value = "date", required = true)
                     @NotNull
                     final OffsetDateTime date,
                     @JsonProperty(value = "price", required = true)
                     @NotNull
                     final BigDecimal price,
                     @JsonProperty(value = "quantity", required = true)
                     final int quantity) {
            this.tradeId = tradeId;
            this.date = date;
            this.price = price;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Trade(");
            sb.append("tradeId='").append(tradeId).append('\'');
            sb.append(", date=").append(date);
            sb.append(", price=").append(price);
            sb.append(", quantity=").append(quantity);
            sb.append(')');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Operation(");
        sb.append("id='").append(id).append('\'');
        sb.append(", status=").append(status);
        if (Objects.nonNull(trades)) sb.append(", trades=").append(trades);
        if (Objects.nonNull(commission)) sb.append(", commission=").append(commission);
        sb.append(", currency=").append(currency);
        sb.append(", payment=").append(payment);
        if (Objects.nonNull(price)) sb.append(", price=").append(price);
        if (Objects.nonNull(quantity)) sb.append(", quantity=").append(quantity);
        if (Objects.nonNull(figi)) sb.append(", figi='").append(figi).append('\'');
        if (Objects.nonNull(instrumentType)) sb.append(", instrumentType=").append(instrumentType);
        sb.append(", isMarginCall=").append(isMarginCall);
        sb.append(", date=").append(date);
        if (Objects.nonNull(operationType)) sb.append(", operationType=").append(operationType);
        sb.append(')');
        return sb.toString();
    }
}
