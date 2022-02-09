package ru.tinkoff.piapi.core.utils;

import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;

public class MapperUtils {


  public static Quotation bigDecimalToQuotation(BigDecimal value) {
    return Quotation.newBuilder()
      .setUnits(getUnits(value))
      .setNano(getNano(value))
      .build();
  }

  public static MoneyValue bigDecimalToMoneyValue(BigDecimal value, String currency) {
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

  public static MoneyValue bigDecimalToMoneyValue(BigDecimal value) {
    return bigDecimalToMoneyValue(value, null);
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
