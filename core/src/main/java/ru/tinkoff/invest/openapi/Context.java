package ru.tinkoff.invest.openapi;

import org.jetbrains.annotations.NotNull;

/**
 * Базовый интерфейс для работы с OpenAPI.
 */
public interface Context {

    /**
     * Получение пути URL, в котором ведётся работа.
     *
     * @return Путь в URL.
     */
    @NotNull
    String getPath();

}
