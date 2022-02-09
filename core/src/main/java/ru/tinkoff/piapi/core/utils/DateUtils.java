package ru.tinkoff.piapi.core.utils;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtils {

  private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Etc/GMT");
  private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;

  /**
   * Преобразование java {@link Long} в google {@link Timestamp}.
   *
   * @param epochSeconds Экземпляр {@link Long}.
   * @return Эквивалентный {@link Timestamp}.
   */
  public static Timestamp epochToTimestamp(Long epochSeconds) {
    return Timestamp.newBuilder().setSeconds(epochSeconds).build();
  }

  /**
   * Преобразование java {@link OffsetDateTime} в google {@link Timestamp}.
   *
   * @param offsetDateTime Экземпляр {@link OffsetDateTime}.
   * @return Эквивалентный {@link Timestamp}.
   */
  public static Timestamp offsetDateTimeToTimestamp(OffsetDateTime offsetDateTime) {
    Instant instant = offsetDateTime.toInstant();

    return Timestamp.newBuilder()
      .setSeconds(instant.getEpochSecond())
      .setNanos(instant.getNano())
      .build();
  }

  /**
   * Преобразование java {@link Timestamp} в java {@link LocalDate}.
   *
   * @param timestamp Экземпляр google {@link Timestamp}.
   * @return Эквивалентный {@link LocalDate}.
   */
  public static LocalDate epochSecondToLocalDate(Timestamp timestamp) {
    return Instant.ofEpochMilli(timestamp.getSeconds() * 1000).atZone(DEFAULT_ZONE_OFFSET).toLocalDate();
  }

  /**
   * Преобразование java {@link OffsetDateTime} в java {@link Long}.
   *
   * @param offsetDateTime Экземпляр google {@link OffsetDateTime}.
   * @return Эквивалентный {@link Long}.
   */
  public static Long offsetDateTimeToLong(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }

    return offsetDateTime.toInstant().getEpochSecond();
  }

  /**
   * Преобразование java {@link Instant} в google {@link Timestamp}.
   *
   * @param i Экземпляр {@link Instant}.
   * @return Эквивалентный {@link Timestamp}.
   */
  public static Timestamp instantToTimestamp(Instant i) {
    return Timestamp.newBuilder()
      .setSeconds(i.getEpochSecond())
      .setNanos(i.getNano())
      .build();
  }

  /**
   * Преобразование google {@link Timestamp} в java {@link Instant}.
   *
   * @param t Экземпляр {@link Timestamp}.
   * @return Эквивалентный {@link Instant}.
   */
  public static Instant timestampToInstant(Timestamp t) {
    return Instant.ofEpochSecond(t.getSeconds(), t.getNanos());
  }

  /**
   * Возвращает текстовое представление даты в виде 2021-09-27T11:05:27Z (GMT)
   * @param epochMillis Время в миллисекундах в epoch формате
   * @return текстовое представление даты в виде 2021-09-27T11:05:27Z
   */
  public static String epochMillisToString(long epochMillis) {
    var zonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(DEFAULT_ZONE_ID);
    return zonedDateTime.format(DateTimeFormatter.ofPattern(PATTERN));
  }

  /**
   * Возвращает текстовое представление даты в виде 2021-09-27T11:05:27Z (GMT)
   * @param timestamp Время в формате Timestamp (google)
   * @return текстовое представление даты в виде 2021-09-27T11:05:27Z
   */
  public static String timestampToString(Timestamp timestamp) {
    return epochMillisToString(timestamp.getSeconds() * 1_000);
  }
}
