package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Операция с инструментом производённая брокером.
 */
public class Operation {

    /**
     * Идентификатор операции.
     */
    private final String id;

    /**
     * Статус операции.
     */
    private final OperationStatus status;

    /**
     * Сделки, произведённые в рамках операции.
     */
    private final List<OperationTrade> trades;

    /**
     * Размер коммиссии.
     */
    private final MoneyAmount commission;

    /**
     * Валюта, в которой производится операция.
     */
    private final Currency currency;

    /**
     * Полная сумма операции.
     */
    private final BigDecimal payment;

    /**
     * Цена за единицу.
     */
    private final BigDecimal price;

    /**
     * Объём операции.
     */
    private final int quantity;

    /**
     * Идентификатор инструмента.
     */
    private final String figi;

    /**
     * Тип инструмента.
     */
    private final InstrumentType instrumentType;

    /**
     * Флаг маржинальной торговли.
     */
    private final boolean isMarginCall;

    /**
     * Дата/время исполнения операции.
     */
    private final OffsetDateTime date;

    /**
     * Тип операции.
     */
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
                     OffsetDateTime date,
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
    public OffsetDateTime getDate() {
        return date;
    }

    /**
     * Получение типа операции.
     */
    public ExtendedOperationType getOperationType() {
        return operationType;
    }
}
