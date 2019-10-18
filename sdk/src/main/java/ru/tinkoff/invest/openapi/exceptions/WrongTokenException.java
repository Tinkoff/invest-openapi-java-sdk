package ru.tinkoff.invest.openapi.exceptions;

/**
 * Исключение возникающее при попытке использовать неверный авторизационный токен.
 */
public class WrongTokenException extends Exception {
    public WrongTokenException() {
        super("Попытка использовать неверный токен");
    }
}
