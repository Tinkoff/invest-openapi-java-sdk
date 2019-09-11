package ru.tinkoff.trading.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Операция с инструментов производимая брокером.
 */
public class Operation {
    private final String id;
    private final OperationStatus status;
    private final List<OperationTrade> trades;
    private final MoneyAmount commission;
    private final Currency currency;
    private final BigDecimal payment;
    private final BigDecimal price;
    private final int quantity;
    private final String figi;
    private final InstrumentType instrumentType;
    private final boolean isMarginCall;
    private final ZonedDateTime date;
    private final ExtendedOperationType operationType;

    @JsonCreator
    public Operation(@JsonProperty("id")
                     String id,
                     @JsonProperty("status")
                     OperationStatus status,
                     @JsonProperty("trades")
                     List<OperationTrade> trades,
                     @JsonProperty("commission")
                     MoneyAmount commission,
                     @JsonProperty("currency")
                     Currency currency,
                     @JsonProperty("payment")
                     BigDecimal payment,
                     @JsonProperty("price")
                     BigDecimal price,
                     @JsonProperty("quantity")
                     int quantity,
                     @JsonProperty("figi")
                     String figi,
                     @JsonProperty("instrumentType")
                     InstrumentType instrumentType,
                     @JsonProperty("isMarginCall")
                     boolean isMarginCall,
                     @JsonProperty("date")
                     ZonedDateTime date,
                     @JsonProperty("operationType")
                     ExtendedOperationType operationType) {
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
     * Получение идентификатора заявки, на основании которой была инициирована операция.
     */
    public String getId() {
        return id;
    }

    /**
     * Получение статуса операции.
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * Получение списока сделок произвёднных на бирже в рамках операции.
     * Может быть null.
     */
    public List<OperationTrade> getTrades() {
        return trades;
    }

    /**
     * Получение коммиссии за операцию.
     * Может быть null.
     */
    public MoneyAmount getCommission() {
        return commission;
    }

    /**
     * Получение валюты исползуемой в операции.
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Получение полной стоимости операции операции (без коммиссии).
     */
    public BigDecimal getPayment() {
        return payment;
    }

    /**
     * Получение цены за инструмент в рамках операции.
     * Может быть null.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Получение количества оперируемого инструмента в рамках операции.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Получение идентификатора оперируемого инструмента в рамках операции.
     * Может быть null.
     */
    public String getFigi() {
        return figi;
    }

    /**
     * Получение типа оперируемого инструмента в рамках операции.
     * Может быть null.
     */
    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    /**
     * Получение признака того что операция является маржинальной.
     */
    public boolean isMarginCall() {
        return isMarginCall;
    }

    /**
     * Получение даты/времени исполнения операции.
     */
    public ZonedDateTime getDate() {
        return date;
    }

    /**
     * Получение типа операции.
     */
    public ExtendedOperationType getOperationType() {
        return operationType;
    }
}
