package ru.tinkoff.invest.openapi.exceptions;

public class WrongTokenException extends Exception {
    public WrongTokenException() {
        super("Попытка использовать неверный токен");
    }
}
