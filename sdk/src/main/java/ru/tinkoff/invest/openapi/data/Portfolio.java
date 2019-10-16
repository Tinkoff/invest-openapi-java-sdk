package ru.tinkoff.invest.openapi.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class Portfolio {

    private final List<PortfolioPosition> positions;

    public static class PortfolioPosition {
        private final String figi;
        private final String ticker;
        private final String isin;
        private final InstrumentType instrumentType;
        private final BigDecimal balance;
        private final BigDecimal blocked;
        private final MoneyAmount expectedYield;
        private final int lots;
        private final MoneyAmount averagePositionPrice;
        private final MoneyAmount averagePositionPriceNoNkd;

        @JsonCreator
        public PortfolioPosition(@JsonProperty("positions")
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
                                 @JsonProperty("expectedYield")
                                 MoneyAmount expectedYield,
                                 @JsonProperty("lots")
                                 int lots,
                                 @JsonProperty("averagePositionPrice")
                                 MoneyAmount averagePositionPrice,
                                 @JsonProperty("averagePositionPriceNoNkd")
                                 MoneyAmount averagePositionPriceNoNkd) {
            this.figi = figi;
            this.ticker = ticker;
            this.isin = isin;
            this.instrumentType = instrumentType;
            this.balance = balance;
            this.blocked = blocked;
            this.expectedYield = expectedYield;
            this.lots = lots;
            this.averagePositionPrice = averagePositionPrice;
            this.averagePositionPriceNoNkd = averagePositionPriceNoNkd;
        }

        public String getFigi() {
            return figi;
        }

        public String getTicker() {
            return ticker;
        }

        public String getIsin() {
            return isin;
        }

        public InstrumentType getInstrumentType() {
            return instrumentType;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getBlocked() {
            return blocked;
        }

        public MoneyAmount getExpectedYield() {
            return expectedYield;
        }

        public int getLots() {
            return lots;
        }

        public MoneyAmount getAveragePositionPrice() {
            return averagePositionPrice;
        }

        public MoneyAmount getAveragePositionPriceNoNkd() {
            return averagePositionPriceNoNkd;
        }
    }

    @JsonCreator
    public Portfolio(@JsonProperty("positions")
                     List<PortfolioPosition> positions) {
        this.positions = positions;
    }

    public List<PortfolioPosition> getPositions() {
        return positions;
    }

}
