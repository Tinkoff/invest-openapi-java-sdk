package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.*;

import java.util.concurrent.Flow;

/**
 * Интерфейс стратегии торгового робота. Стратегия это чёрный ящщик, который на вход принимает исследуемый инструмент
 * и поток биржевых данных, а на выходе подаёт сигналы о размещении или отмене лимитных заявок.
 */
public interface Strategy extends Flow.Processor<TradingState, StrategyDecision> {

    /**
     * Получение исследуемого инструмента.
     *
     * @return Инструмент.
     */
    Instrument getInstrument();

    /**
     * Получение разрешения, с которым принимаются потоковые данные с биржи.
     *
     * @return Разрешение.
     */
    CandleInterval getCandleInterval();

    /**
     * Максимальная глубина заявочного стакана, в которую смотрит стратегия.
     *
     * @return Глубина стакана.
     */
    int getOrderbookDepth();

    /**
     * Выполнение подготовительных действий.
     */
    void init();

    /**
     * Выполнение завершающих действий (при остановке работы).
     */
    void cleanup();

    /**
     * Получение текущего состояния торговой ситуации.
     *
     * @return Состояние торговой ситуации.
     */
    TradingState getCurrentState();

}
