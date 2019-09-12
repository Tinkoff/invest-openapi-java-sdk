package ru.tinkoff.invest.openapi.wrapper;

import java.net.http.HttpClient;
import java.net.http.WebSocket;

/**
 * Интерфейс подключения к OpenAPI.
 *
 * @param <C> Вариант контекста.
 */
public interface Connection<C extends Context> {

    /**
     * Получение контекста.
     *
     * @return Контекст OpenApi.
     */
    C context();

    /**
     * URL-строки хоста rest-составляющей OpenAPI.
     *
     * @return URL-строка хоста.
     */
    String getHost();

    /**
     * Авторизационный токен.
     *
     * @return SSO-токен.
     */
    String getAuthToken();

    /**
     * Http-клиент, используемый для взаимодействия с rest-состовляющей OpenAPI.
     *
     * @return Http-клиент.
     */
    HttpClient getHttpClient();

    /**
     * WebSocket-клиент, используемый для получения потока рыночной информации.
     *
     * @return WebSocket-клиент.
     */
    WebSocket getWebSocket();

    /**
     * Обработчик сообщений получаемых через WebSocket-клиент.
     *
     * @return Обработчик сообщений.
     */
    WebSocketListener getListener();

}
