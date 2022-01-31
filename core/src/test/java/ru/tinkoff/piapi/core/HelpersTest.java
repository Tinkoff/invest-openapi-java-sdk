package ru.tinkoff.piapi.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.google.protobuf.Timestamp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.smallrye.mutiny.Multi;

public class HelpersTest {

  @Test
  void wrapWithFuture_Test() {
    var expected = "sample";

    var future = Helpers.<String>wrapWithFuture(observer -> {
      observer.onNext(expected);
      observer.onCompleted();
    });

    assertEquals(expected, future.join());
  }

  @Test
  void instantToTimestamp_Test() {
    var input = Instant.ofEpochSecond(1234567890, 111222333);
    var expected = Timestamp.newBuilder()
      .setSeconds(input.getEpochSecond())
      .setNanos(input.getNano())
      .build();

    var actual = Helpers.instantToTimestamp(input);

    assertEquals(expected, actual);
  }

  @Test
  void timestampToInstant_Test() {
    var input = Timestamp.newBuilder()
      .setSeconds(1234567890)
      .setNanos(111222333)
      .build();
    var expected = Instant.ofEpochSecond(input.getSeconds(), input.getNanos());

    var actual = Helpers.timestampToInstant(input);

    assertEquals(expected, actual);
  }

  @Test
  void wrapEmitterWithStreamObserver_Test() {
    var expected = List.of("sample");
    var actual = new LinkedList<String>();

    Multi.createFrom()
      .<String>emitter(emitter -> {
        var observer = Helpers.<String>wrapEmitterWithStreamObserver(emitter);
        for (var item : expected) {
          observer.onNext(item);
        }
      })
      .subscribe()
      .with(actual::add);

    assertIterableEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("stringAndStringProvider")
  void preprocessInputOrderId_Test(String input, String expected) {
    assertEquals(expected, Helpers.preprocessInputOrderId(input));
  }

  static Stream<Arguments> stringAndStringProvider() {
    return Stream.of(
      Arguments.of("test", "test"),
      Arguments.of("abcdefghijklmnopqrstuvwxyz1234567890extra", "abcdefghijklmnopqrstuvwxyz1234567890"),
      Arguments.of("abcdefghijklmnopqrstuvwxyz1234567890", "abcdefghijklmnopqrstuvwxyz1234567890"),
      Arguments.of("", ""),
      Arguments.of(" ", ""),
      Arguments.of("  ", "")
    );
  }

}
