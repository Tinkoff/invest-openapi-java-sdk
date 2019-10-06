package ru.tinkoff.invest.openapi.wrapper;

import java.net.http.WebSocket;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * Обратчик сообщений принимаемых WebSocket-клиентом с возможностью задания отдельного потребителя.
 */
public interface WebSocketListener extends WebSocket.Listener {

    /**
     * Подписка на событие получения очередного сообщения.
     *
     * @param subscriber Подписчик.
     */
    void subscribeOnMessage(Flow.Subscriber<String> subscriber);

    /**
     * Подписывание на событие закрытия WebSocket-соединения.
     *
     * @param subscriber Подписчик.
     */
    void subscribeOnClose(Flow.Subscriber<Void> subscriber);

    /**
     * Подписывание на событие ошибки в работе WebSocket-соединения.
     *
     * @param subscriber Подписчик.
     */
    void subscribeOnError(Flow.Subscriber<Void> subscriber);

}
