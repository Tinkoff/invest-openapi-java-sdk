package ru.tinkoff.invest.openapi;

import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.model.sandbox.PositionBalance;

import java.util.function.BiConsumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся режима "песочницы".
 */
public interface SandboxContext extends Context {

    /**
     * Регистрация в системе "песочницы". Проводится один раз для клиента.
     *
     * @param callback Функция обратного вызова
     */
    void performRegistration(BiConsumer<Void, Throwable> callback);

    /**
     * Установка значения валютного актива.
     *
     * @param data Параметры запроса.
     * @param callback Функция обратного вызова.
     */
    void setCurrencyBalance(CurrencyBalance data, BiConsumer<Void, Throwable> callback);

    /**
     * Установка позиции по инструментну.
     *
     * @param data Параметры запроса.
     * @param callback Функция обратного вызова.
     */
    void setPositionBalance(PositionBalance data, BiConsumer<Void, Throwable> callback);

    /**
     * Сброс всех установленных значений по активам.
     *
     * @param callback Функция обратного вызова
     */
    void clearAll(BiConsumer<Void, Throwable> callback);


}
