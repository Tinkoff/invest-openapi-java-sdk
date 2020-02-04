package ru.tinkoff.invest.openapi.automata;

import java.math.BigDecimal;

/**
 * Интерфейс стратегии торгового робота. Стратегия это чёрный ящщик, который на вход принимает исследуемый инструмент
 * и поток биржевых данных, а на выходе подаёт сигналы о размещении или отмене лимитных заявок.
 */
public interface Strategy {


    /**
     * Получение разрешения, с которым принимаются потоковые данные с биржи.
     *
     * @return Разрешение.
     */
    TradingState.Candle.CandleInterval getCandleInterval();

    /**
     * Максимальная глубина заявочного стакана, в которую смотрит стратегия.
     *
     * @return Глубина стакана.
     */
    int getOrderbookDepth();

    /**
     * Получение текущего состояния торговой ситуации.
     *
     * @return Состояние торговой ситуации.
     */
    TradingState getCurrentState();

    StrategyDecision handleNewState(TradingState newState);

    Instrument getInstrument();
}
