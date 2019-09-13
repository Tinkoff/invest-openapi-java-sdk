package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.data.*;

/**
 * Интерфейс стратегии торгового робота. Стратегия это чёрный ящщик, который на вход принимает исследуемый инструмент
 * и поток биржевых данных, а на выходе подаёт сигналы о размещении или отмене лимитных заявок.
 */
public interface Strategy {

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
     * Обработка размещённой заявки.
     *
     * @param placedLimitOrder Размещённая заяка.
     */
    void consumePlacedLimitOrder(PlacedLimitOrder placedLimitOrder);

    /**
     * Обработка неразмещённой заявки.
     *
     * @param operationType Тип заявки.
     */
    void consumeRejectedLimitOrder(OperationType operationType);

    /**
     * Выполнение подготовительных действий.
     */
    void init();

    /**
     * Формирование реакции на изменение рыночной ситуации.
     *
     * @param state Информация о состоянии рынка.
     * @return Решение по ситуации.
     */
    StrategyDecision reactOnMarketChange(TradingState state);

}
