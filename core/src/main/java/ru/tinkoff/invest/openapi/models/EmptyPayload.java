package ru.tinkoff.invest.openapi.models;

/**
 * Модель пустого ответа.
 */
public final class EmptyPayload {
    private static final EmptyPayload singleton;

    static {
        singleton = new EmptyPayload();
    }

    private EmptyPayload() {
    }

    /**
     * Получить экземпляр модели.
     *
     * @return Экземпляр пустого ответа.
     */
    public static EmptyPayload instance() {
        return EmptyPayload.singleton;
    }

    @Override
    public String toString() {
        return "EmptyPayload";
    }
}
