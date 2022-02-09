package ru.tinkoff.piapi.core.utils;

import com.google.protobuf.Timestamp;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class MapperUtils {

  private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;

  public static Timestamp mapTimestamp(Long epochSeconds) {
    return Timestamp.newBuilder().setSeconds(epochSeconds).build();
  }

  public static Timestamp mapTimestamp(Instant instant) {
    return Timestamp.newBuilder()
      .setSeconds(instant.getEpochSecond())
      .setNanos(instant.getNano())
      .build();
  }

  public static Timestamp mapTimestamp(OffsetDateTime offsetDateTime) {
    Instant instant = offsetDateTime.toInstant();

    return Timestamp.newBuilder()
      .setSeconds(instant.getEpochSecond())
      .setNanos(instant.getNano())
      .build();
  }

  public static Instant toInstant(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }

  public static LocalDate epochSecondToLocalDate(Timestamp timestamp) {
    return Instant.ofEpochMilli(timestamp.getSeconds() * 1000).atZone(DEFAULT_ZONE_OFFSET).toLocalDate();
  }

  public static Long convertOffsetDateTimeToLong(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }

    return offsetDateTime.toInstant().getEpochSecond();
  }

  public static Quotation mapQuotation(BigDecimal value) {
    return Quotation.newBuilder()
      .setUnits(getUnits(value))
      .setNano(getNano(value))
      .build();
  }

  public static MoneyValue mapMoney(BigDecimal value, String currency) {
    return MoneyValue.newBuilder()
      .setUnits(getUnits(value))
      .setNano(getNano(value))
      .setCurrency(toLowerCaseNullable(currency))
      .build();
  }

  public static long getUnits(BigDecimal value) {
    return value != null ? value.longValue() : 0;
  }

  public static int getNano(BigDecimal value) {
    return value != null ? value.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(1_000_000_000L)).intValue() : 0;
  }

  private static String toLowerCaseNullable(String value) {
    return value != null ? value.toLowerCase() : "";
  }

  public static MoneyValue mapMoney(BigDecimal value) {
    return mapMoney(value, null);
  }

  /**
   * Конвертирует Quotation в BigDecimal. Например {units: 10, nanos: 900000000} -> 10.9
   *
   * @param value значение в формате Quotation
   * @return Значение в формате BigDecimal
   */
  public static BigDecimal quotationToBigDecimal(Quotation value) {
    if (value == null) {
      return null;
    }
    return mapUnitsAndNanos(value.getUnits(), value.getNano());
  }

  /**
   * Конвертирует MoneyValue в BigDecimal. Например {units: 10, nanos: 900000000, currency: 'rub'} -> 10.9
   *
   * @param value значение в формате MoneyValue
   * @return Значение в формате BigDecimal
   */
  public static BigDecimal moneyValueToBigDecimal(MoneyValue value) {
    if (value == null) {
      return null;
    }
    return mapUnitsAndNanos(value.getUnits(), value.getNano());
  }

  public static BigDecimal mapUnitsAndNanos(long units, int nanos) {
    if (units == 0 && nanos == 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(units).add(BigDecimal.valueOf(nanos, 9));
  }
}
