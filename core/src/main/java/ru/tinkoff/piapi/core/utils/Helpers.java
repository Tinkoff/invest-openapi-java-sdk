package ru.tinkoff.piapi.core.utils;

import io.grpc.stub.StreamObserver;
import io.smallrye.mutiny.subscription.MultiEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Helpers {

  /**
   * Связывание асинхронного Unary-вызова с {@link CompletableFuture}.
   *
   * @param callPerformer Асинхронный Unary-вызов.
   * @param <T>           Тип результата вызова.
   * @return {@link CompletableFuture} с результатом вызова.
   */
  public static <T> CompletableFuture<T> wrapWithFuture(
    Consumer<StreamObserver<T>> callPerformer) {
    var cf = new CompletableFuture<T>();
    callPerformer.accept(mkStreamObserverWithFuture(cf));
    return cf;
  }

  /**
   * Создание StreamObserver, который связывает свой результат с CompletableFuture.
   * <p>
   * Только для Unary-вызовов!
   */
  private static <T> StreamObserver<T> mkStreamObserverWithFuture(CompletableFuture<T> cf) {
    return new StreamObserver<>() {
      @Override
      public void onNext(T value) {
        cf.complete(value);
      }

      @Override
      public void onError(Throwable t) {
        cf.completeExceptionally(t);
      }

      @Override
      public void onCompleted() {
      }
    };
  }

  /**
   * Связывание {@link MultiEmitter} со {@link StreamObserver}.
   *
   * @param emitter Экземпляр {@link MultiEmitter}.
   * @param <T>     Тип оперируемый {@link MultiEmitter}.
   * @return Связанный {@link StreamObserver}.
   */
  public static <T> StreamObserver<T> wrapEmitterWithStreamObserver(MultiEmitter<? super T> emitter) {
    return new StreamObserver<>() {
      @Override
      public void onNext(T value) {
        emitter.emit(value);
      }

      @Override
      public void onError(Throwable t) {
        emitter.fail(t);
      }

      @Override
      public void onCompleted() {
        emitter.complete();
      }
    };
  }

  /**
   * Проведение необходимых преобразований для пользовательского идентификатора поручения.
   *
   * @param orderId Пользовательский идентификатор поручения.
   * @return Преобразованный идентификатор поручения.
   */
  public static String preprocessInputOrderId(String orderId) {
    var maxLength = Math.min(orderId.length(), 36);
    return orderId.isBlank() ? orderId.trim() : orderId.substring(0, maxLength);
  }

}
