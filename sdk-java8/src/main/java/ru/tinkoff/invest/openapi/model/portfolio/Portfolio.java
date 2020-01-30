package ru.tinkoff.invest.openapi.model.portfolio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tinkoff.invest.openapi.model.MoneyAmount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Модель портфеля инструментов.
 */
public class Portfolio {

    /**
     * Список позиций.
     */
    public final List<PortfolioPosition> positions;

    @JsonCreator
    public Portfolio(@JsonProperty("positions")
                             List<PortfolioPosition> positions) {
        if (Objects.isNull(positions)) {
            throw new IllegalArgumentException("Список позиций не может быть null.");
        }

        this.positions = positions;
    }

    /**
     * Модель позиции в портфеле.
     */
    public static class PortfolioPosition {

        /**
         * Идентификатор инструмента.
         */
        public final String figi;

        /**
         * Краткий биржевой идентификатор ("тикер").
         * Может быть null.
         */
        public final String ticker;

        /**
         * Международный идентификационный код ценной бумаги.
         * Может быть null.
         */
        public final String isin;

        /**
         * Тип инструмента.
         */
        public final InstrumentType instrumentType;

        /**
         * Объём позиции.
         */
        public final BigDecimal balance;

        /**
         * Заблокированный объём.
         * Может быть null.
         */
        public final BigDecimal blocked;

        /**
         * Количество лотов.
         */
        public final Integer lots;

        /**
         * Ожидаемая доходность.
         * Может быть null.
         */
        public final MoneyAmount expectedYield;

        /**
         * Средняя цена позиции.
         * Может быть null.
         */
        public final MoneyAmount averagePositionPrice;

        /**
         * Средняя цена без учёта НКД.
         * Может быть null.
         */
        public final MoneyAmount averagePositionPriceNoNkd;

        @JsonCreator
        public PortfolioPosition(@JsonProperty("figi")
                                         String figi,
                                 @JsonProperty("ticker")
                                         String ticker,
                                 @JsonProperty("isin")
                                         String isin,
                                 @JsonProperty("instrumentType")
                                         InstrumentType instrumentType,
                                 @JsonProperty("balance")
                                         BigDecimal balance,
                                 @JsonProperty("blocked")
                                         BigDecimal blocked,
                                 @JsonProperty("lots")
                                         Integer lots,
                                 @JsonProperty("expectedYield")
                                         MoneyAmount expectedYield,
                                 @JsonProperty("averagePositionPrice")
                                         MoneyAmount averagePositionPrice,
                                 @JsonProperty("averagePositionPriceNoNkd")
                                         MoneyAmount averagePositionPriceNoNkd) {
            if (Objects.isNull(figi)) {
                throw new IllegalArgumentException("Идентификатор инструмента не может быть null.");
            }
            if (Objects.isNull(instrumentType)) {
                throw new IllegalArgumentException("Тип инструмента не может быть null.");
            }
            if (Objects.isNull(balance)) {
                throw new IllegalArgumentException("Значение баланса не может быть null.");
            }
            if (Objects.isNull(lots)) {
                throw new IllegalArgumentException("Количество лотов не может быть null.");
            }

            this.figi = figi;
            this.ticker = ticker;
            this.isin = isin;
            this.instrumentType = instrumentType;
            this.balance = balance;
            this.blocked = blocked;
            this.lots = lots;
            this.expectedYield = expectedYield;
            this.averagePositionPrice = averagePositionPrice;
            this.averagePositionPriceNoNkd = averagePositionPriceNoNkd;
        }

    }

}
