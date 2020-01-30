package ru.tinkoff.invest.openapi.automata;


import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель биржевого инструмента.
 */
public class Instrument {

    public final String figi;
    public final BigDecimal minPriceIncrement;
    public final Integer lot;
    public final TradingState.Currency currency;

    public Instrument(String figi,
                      BigDecimal minPriceIncrement,
                      int lot,
                      TradingState.Currency currency) {
        if (Objects.isNull(figi)) {
            throw new IllegalArgumentException("Идентификатор не может быть null.");
        }

        this.figi = figi;
        this.minPriceIncrement = minPriceIncrement;
        this.lot = lot;
        this.currency = currency;
    }

}
