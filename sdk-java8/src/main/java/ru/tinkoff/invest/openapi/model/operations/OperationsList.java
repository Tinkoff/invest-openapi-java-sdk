package ru.tinkoff.invest.openapi.model.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tinkoff.invest.openapi.model.Currency;
import ru.tinkoff.invest.openapi.model.MoneyAmount;
import ru.tinkoff.invest.openapi.model.portfolio.InstrumentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Модель списка операций возвращаемая OpenAPI.
 */
public class OperationsList {

    /**
     * Непосредственно список операций.
     */
    public final List<Operation> operations;

    @JsonCreator
    public OperationsList(@JsonProperty("operations")
                                  List<Operation> operations) {
        if (Objects.isNull(operations)) {
            throw new IllegalArgumentException("Список операций не может быть null.");
        }

        this.operations = operations;
    }

    /**
     * Операция с инструментом производённая брокером.
     */
    public static class Operation {

        /**
         * Идентификатор операции.
         */
        public final String id;

        /**
         * Статус операции.
         */
        public final Status status;

        /**
         * Сделки, произведённые в рамках операции.
         * Может быть null.
         */
        public final List<Trade> trades;

        /**
         * Размер коммиссии.
         * Может быть null.
         */
        public final MoneyAmount commission;

        /**
         * Валюта, в которой производится операция.
         */
        public final Currency currency;

        /**
         * Полная сумма операции.
         */
        public final BigDecimal payment;

        /**
         * Цена за единицу.
         * Может быть null.
         */
        public final BigDecimal price;

        /**
         * Объём операции.
         * Может быть null.
         */
        public final Integer quantity;

        /**
         * Идентификатор инструмента.
         * Может быть null.
         */
        public final String figi;

        /**
         * Тип инструмента.
         * Может быть null.
         */
        public final InstrumentType instrumentType;

        /**
         * Флаг маржинальной торговли.
         */
        public final Boolean isMarginCall;

        /**
         * Дата/время исполнения операции.
         */
        public final OffsetDateTime date;

        /**
         * Тип операции.
         * Может быть null.
         */
        public final OperationType operationType;

        @JsonCreator
        public Operation(@JsonProperty("id")
                                 String id,
                         @JsonProperty("status")
                                 Status status,
                         @JsonProperty("trades")
                                 List<Trade> trades,
                         @JsonProperty("commission")
                                 MoneyAmount commission,
                         @JsonProperty("currency")
                                 Currency currency,
                         @JsonProperty("payment")
                                 BigDecimal payment,
                         @JsonProperty("price")
                                 BigDecimal price,
                         @JsonProperty("quantity")
                                 Integer quantity,
                         @JsonProperty("figi")
                                 String figi,
                         @JsonProperty("instrumentType")
                                 InstrumentType instrumentType,
                         @JsonProperty("isMarginCall")
                                 Boolean isMarginCall,
                         @JsonProperty("date")
                                 OffsetDateTime date,
                         @JsonProperty("operationType")
                                 OperationType operationType) {
            if (Objects.isNull(id)) {
                throw new IllegalArgumentException("Идентификатор не может быть null.");
            }
            if (Objects.isNull(status)) {
                throw new IllegalArgumentException("Статус не может быть null.");
            }
            if (Objects.isNull(currency)) {
                throw new IllegalArgumentException("Валюта не может быть null.");
            }
            if (Objects.isNull(payment)) {
                throw new IllegalArgumentException("Полная сумма не может быть null.");
            }
            if (Objects.isNull(isMarginCall)) {
                throw new IllegalArgumentException("Флаг маржинальной торговли не может быть null.");
            }
            if (Objects.isNull(date)) {
                throw new IllegalArgumentException("Дата/время операции не может быть null.");
            }

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
         * Возможные статусы операции.
         */
        public enum Status {
            Done, Decline, Progress
        }

        /**
         * Модель сделки в рамках операции.
         */
        public class Trade {

            /**
             * Идентификатор сделки.
             */
            private final String tradeId;

            /**
             * Дата/время совершения сделки.
             */
            private final OffsetDateTime date;

            /**
             * Цена за единицу.
             */
            private final BigDecimal price;

            /**
             * Объём сделки.
             */
            private final int quantity;

            @JsonCreator
            public Trade(@JsonProperty("tradeId")
                                 String tradeId,
                         @JsonProperty("date")
                                 OffsetDateTime date,
                         @JsonProperty("price")
                                 BigDecimal price,
                         @JsonProperty("quantity")
                                 int quantity) {
                this.tradeId = tradeId;
                this.date = date;
                this.price = price;
                this.quantity = quantity;
            }

            public String getTradeId() {
                return tradeId;
            }

            public OffsetDateTime getDate() {
                return date;
            }

            public BigDecimal getPrice() {
                return price;
            }

            public int getQuantity() {
                return quantity;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Trade)) {
                    return false;
                }
                final Trade other = (Trade)o;

                return this.tradeId.equals(other.tradeId) &&
                        this.date.equals(other.date) &&
                        this.price.equals(other.price) &&
                        this.quantity == other.quantity;
            }
        }

    }

}
