package ru.tinkoff.invest.openapi.exceptions;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderAlreadyCancelledException extends OpenApiException {

    private static final long serialVersionUID = -5527556849561779960L;

    private static Pattern orderIdExtractionPattern = Pattern.compile("^.+id (.+)$");

    private final String orderId;

    public OrderAlreadyCancelledException(@NotNull final String message,
                                          @NotNull final String code) {
        super(message, code);

        final Matcher matchResult = orderIdExtractionPattern.matcher(message);
        this.orderId = matchResult.group(matchResult.groupCount());
    }

    public String getCurrency() {
        return orderId;
    }

}
