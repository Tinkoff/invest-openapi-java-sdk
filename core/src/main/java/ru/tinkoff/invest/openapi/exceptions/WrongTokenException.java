package ru.tinkoff.invest.openapi.exceptions;

/**
 * Исключение возникающее при попытке использовать неверный авторизационный токен.
 */
public class WrongTokenException extends OpenApiException {

    public static String ACCESS_DENIED_MESSAGE_CODE = "ACCESS_DENIED";

    public WrongTokenException() {
        super("Попытка использовать неверный токен", ACCESS_DENIED_MESSAGE_CODE);
    }

}
