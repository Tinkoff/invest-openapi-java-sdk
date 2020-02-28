package ru.tinkoff.invest.openapi.exceptions;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotEnoughBalanceException extends OpenApiException {

    private static final long serialVersionUID = -4135428712268662987L;

    private static Pattern currencyExtractionPattern = Pattern.compile("^.+=(\\w+)$");

    private final String currency;

    public NotEnoughBalanceException(@NotNull final String message,
                                     @NotNull final String code) {
        super(message, code);

        final Matcher matchResult = currencyExtractionPattern.matcher(message);
        this.currency = matchResult.group(matchResult.groupCount());
    }

    public String getCurrency() {
        return currency;
    }

}
