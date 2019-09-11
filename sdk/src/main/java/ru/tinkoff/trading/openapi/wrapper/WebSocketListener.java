package ru.tinkoff.trading.openapi.wrapper;

import java.net.http.WebSocket;
import java.util.function.Consumer;

/**
 * Обратчик сообщений принимаемых WebSocket-клиентом с возможностью задания отдельного потребителя.
 */
public interface WebSocketListener extends WebSocket.Listener {

    /**
     * Установка отдельного потребителя текстовых сообщений.
     *
     * @param messageHandler Потребитель текстовых сообщений.
     */
    void setMessageHandler(Consumer<String> messageHandler);

}
