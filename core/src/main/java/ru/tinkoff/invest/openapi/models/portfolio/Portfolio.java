package ru.tinkoff.invest.openapi.models.portfolio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.MoneyAmount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Модель портфеля инструментов.
 */
public final class Portfolio {

    /**
     * Список позиций.
     */
    @NotNull
    public final List<PortfolioPosition> positions;

    @JsonCreator
    public Portfolio(@JsonProperty("positions")
                     @NotNull
                     final List<PortfolioPosition> positions) {
        this.positions = positions;
    }

    /**
     * Модель позиции в портфеле.
     */
    public final static class PortfolioPosition {

        /**
         * Идентификатор инструмента.
         */
        @NotNull
        public final String figi;

        /**
         * Краткий биржевой идентификатор ("тикер").
         */
        @Nullable
        public final String ticker;

        /**
         * Международный идентификационный код ценной бумаги.
         */
        @Nullable
        public final String isin;

        /**
         * Тип инструмента.
         */
        @NotNull
        public final InstrumentType instrumentType;

        /**
         * Объём позиции.
         */
        @NotNull
        public final BigDecimal balance;

        /**
         * Заблокированный объём.
         */
        @Nullable
        public final BigDecimal blocked;

        /**
         * Ожидаемая доходность.
         */
        @Nullable
        public final MoneyAmount expectedYield;

        /**
         * Количество лотов.
         */
        public final int lots;

        /**
         * Средняя цена позиции.
         */
        @Nullable
        public final MoneyAmount averagePositionPrice;

        /**
         * Средняя цена без учёта НКД.
         */
        @Nullable
        public final MoneyAmount averagePositionPriceNoNkd;

        /**
         * Человеческое имя инструмента.
         */
        @NotNull
        public final String name;

        @JsonCreator
        public PortfolioPosition(@JsonProperty(value = "figi", required = true)
                                 @NotNull
                                 final String figi,
                                 @JsonProperty("ticker")
                                 @Nullable
                                 final String ticker,
                                 @JsonProperty("isin")
                                 @Nullable
                                 final String isin,
                                 @JsonProperty(value = "instrumentType", required = true)
                                 @NotNull
                                 final InstrumentType instrumentType,
                                 @JsonProperty(value = "balance", required = true)
                                 @NotNull
                                 final BigDecimal balance,
                                 @JsonProperty("blocked")
                                 @Nullable
                                 final BigDecimal blocked,
                                 @JsonProperty("expectedYield")
                                 @Nullable
                                 final MoneyAmount expectedYield,
                                 @JsonProperty("lots")
                                 final int lots,
                                 @JsonProperty("averagePositionPrice")
                                 @Nullable
                                 final MoneyAmount averagePositionPrice,
                                 @JsonProperty("averagePositionPriceNoNkd")
                                 @Nullable
                                 final MoneyAmount averagePositionPriceNoNkd,
                                 @JsonProperty(value = "name", required = true)
                                 @NotNull
                                 final String name) {
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
            this.name = name;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PortfolioPosition(");
            sb.append("figi='").append(figi).append('\'');
            if (Objects.nonNull(ticker)) sb.append(", ticker='").append(ticker).append('\'');
            if (Objects.nonNull(isin)) sb.append(", isin='").append(isin).append('\'');
            sb.append(", instrumentType=").append(instrumentType);
            sb.append(", balance=").append(balance);
            if (Objects.nonNull(blocked))sb.append(", blocked=").append(blocked);
            if (Objects.nonNull(expectedYield))sb.append(", expectedYield=").append(expectedYield);
            sb.append(", lots=").append(lots);
            if (Objects.nonNull(averagePositionPrice))sb.append(", averagePositionPrice=").append(averagePositionPrice);
            if (Objects.nonNull(averagePositionPriceNoNkd))sb.append(", averagePositionPriceNoNkd=").append(averagePositionPriceNoNkd);
            sb.append(", name='").append(name).append('\'');
            sb.append(')');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Portfolio(");
        sb.append("positions=").append(positions);
        sb.append(')');
        return sb.toString();
    }

}
