package ru.tinkoff.invest.openapi.exceptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderAlreadyCancelledException extends OpenApiException {

    private static Pattern orderIdExtractionPattern = Pattern.compile("^.+id (.+)$");

    private final String orderId;

    public OrderAlreadyCancelledException(final String message, final String code) {
        super(message, code);

        final Matcher matchResult = orderIdExtractionPattern.matcher(message);
        this.orderId = matchResult.group(matchResult.groupCount());
    }

    public String getCurrency() {
        return orderId;
    }
}
