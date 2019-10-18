package ru.tinkoff.invest.openapi.exceptions;

import ru.tinkoff.invest.openapi.data.CandleInterval;

import java.time.OffsetDateTime;

public class BadCandlesSearchingIntervalException extends Exception {
    public BadCandlesSearchingIntervalException(OffsetDateTime allowedStart,
                                                OffsetDateTime allowedEnd,
                                                CandleInterval searchingInterval) {
        super(
                "Недопустимый промежуток времени: ожидался от " +
                allowedStart.toString() +
                " до " +
                allowedEnd.toString() +
                " (" +
                generateAllowedSection(searchingInterval) +
                ")"
        );
    }

    private static String generateAllowedSection(CandleInterval searchingInterval) {
        switch (searchingInterval) {
            case ONE_MIN:
                return "минимальный интервал 1 минута; макимальный 1 день";
            case TWO_MIN:
                return "минимальный интервал 2 минуты; макимальный 1 день";
            case THREE_MIN:
                return "минимальный интервал 3 минуты; макимальный 1 день";
            case FIVE_MIN:
                return "минимальный интервал 5 минут; макимальный 1 день";
            case TEN_MIN:
                return "минимальный интервал 10 минут; макимальный 1 день";
            case QUARTER_HOUR:
                return "минимальный интервал 15 минут; макимальный 1 день";
            case HALF_HOUR:
                return "минимальный интервал 30 минут; макимальный 1 день";
            case HOUR:
                return "минимальный интервал 1 час; макимальный 1 неделя";
            case TWO_HOUR:
                return "минимальный интервал 2 часа; макимальный 2 недели";
            case FOUR_HOUR:
                return "минимальный интервал 4 часа; макимальный 1 месяц";
            case DAY:
                return "минимальный интервал 1 день; макимальный 1 год";
            case WEEK:
                return "минимальный интервал 1 неделя; макимальный 2 года";
            case MONTH:
                return "минимальный интервал 1 месяц; макимальный 10 лет";
        }

        return ""; // impossible code, to satisfy compiler
    }
}
