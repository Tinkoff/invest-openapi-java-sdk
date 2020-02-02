package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;
import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.model.sandbox.PositionBalance;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Интерфейс работы с OpenAPI в части касающейся режима "песочницы".
 */
public interface SandboxContext extends Context {

    /**
     * Регистрация в системе "песочницы". Проводится один раз для клиента.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void performRegistration(@NotNull Consumer<Void> onComplete,
                             @NotNull Consumer<Throwable> onError);

    /**
     * Установка значения валютного актива.
     *
     * @param data Жалаемые параметры позиции.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void setCurrencyBalance(@NotNull CurrencyBalance data,
                            @NotNull Consumer<Void> onComplete,
                            @NotNull Consumer<Throwable> onError);

    /**
     * Установка позиции по инструменту.
     *
     * @param data Жалаемые параметры позиции.
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void setPositionBalance(@NotNull PositionBalance data,
                            @NotNull Consumer<Void> onComplete,
                            @NotNull Consumer<Throwable> onError);

    /**
     * Сброс всех установленных значений по активам.
     *
     * @param onComplete Функция обратного вызова при упешном исполнении.
     * @param onError Функция обратного вызова при возникновении ошибки при исполнении.
     */
    void clearAll(@NotNull Consumer<Void> onComplete,
                  @NotNull Consumer<Throwable> onError);


}
