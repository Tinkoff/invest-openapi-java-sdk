package ru.tinkoff.invest.openapi.exceptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotEnoughBalanceException extends OpenApiException {

    private static Pattern currencyExtractionPattern = Pattern.compile("^.+=(\\w+)$");

    private final String currency;

    public NotEnoughBalanceException(final String message, final String code) {
        super(message, code);

        final Matcher matchResult = currencyExtractionPattern.matcher(message);
        this.currency = matchResult.group(matchResult.groupCount());
    }

    public String getCurrency() {
        return currency;
    }
}
