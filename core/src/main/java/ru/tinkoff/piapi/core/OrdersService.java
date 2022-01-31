package ru.tinkoff.piapi.core;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.BackPressureStrategy;
import org.reactivestreams.FlowAdapters;
import ru.tinkoff.piapi.contract.v1.*;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

public class OrdersService {
  private final OrdersStreamServiceGrpc.OrdersStreamServiceStub ordersStreamStub;
  private final OrdersServiceGrpc.OrdersServiceBlockingStub ordersBlockingStub;
  private final OrdersServiceGrpc.OrdersServiceStub ordersStub;
  private final boolean readonlyMode;

  OrdersService(
    @Nonnull OrdersStreamServiceGrpc.OrdersStreamServiceStub ordersStreamStub,
    @Nonnull OrdersServiceGrpc.OrdersServiceBlockingStub ordersBlockingStub,
    @Nonnull OrdersServiceGrpc.OrdersServiceStub ordersStub,
    boolean readonlyMode) {
    this.ordersStreamStub = ordersStreamStub;
    this.ordersBlockingStub = ordersBlockingStub;
    this.ordersStub = ordersStub;
    this.readonlyMode = readonlyMode;
  }

  @Nonnull
  public Publisher<TradesStreamResponse> tradesStream() {
    var mutinyPublisher = Multi.createFrom().<TradesStreamResponse>emitter(
      emitter -> ordersStreamStub.tradesStream(
        TradesStreamRequest.newBuilder().build(),
        Helpers.wrapEmitterWithStreamObserver(emitter)),
      BackPressureStrategy.BUFFER);

    return FlowAdapters.toFlowPublisher(mutinyPublisher);
  }

  @Nonnull
  public PostOrderResponse postOrderSync(
    @Nonnull String figi,
    long quantity,
    @Nonnull Quotation price,
    @Nonnull OrderDirection direction,
    @Nonnull String accountId,
    @Nonnull OrderType type,
    @Nonnull String orderId) {
    if (readonlyMode) throw new ReadonlyModeViolationException();

    return ordersBlockingStub.postOrder(
      PostOrderRequest.newBuilder()
        .setFigi(figi)
        .setQuantity(quantity)
        .setPrice(price)
        .setDirection(direction)
        .setAccountId(accountId)
        .setOrderType(type)
        .setOrderId(Helpers.preprocessInputOrderId(orderId))
        .build());
  }

  @Nonnull
  public Instant cancelOrderSync(
    @Nonnull String accountId,
    @Nonnull String orderId) {
    if (readonlyMode) throw new ReadonlyModeViolationException();

    var responseTime = ordersBlockingStub.cancelOrder(
        CancelOrderRequest.newBuilder()
          .setAccountId(accountId)
          .setOrderId(orderId)
          .build())
      .getTime();

    return Helpers.timestampToInstant(responseTime);
  }

  @Nonnull
  public OrderState getOrderStateSync(
    @Nonnull String accountId,
    @Nonnull String orderId) {
    return ordersBlockingStub.getOrderState(
      GetOrderStateRequest.newBuilder()
        .setAccountId(accountId)
        .setOrderId(orderId)
        .build());
  }

  @Nonnull
  public List<OrderState> getOrdersSync(@Nonnull String accountId) {
    return ordersBlockingStub.getOrders(
        GetOrdersRequest.newBuilder()
          .setAccountId(accountId)
          .build())
      .getOrdersList();
  }

  @Nonnull
  public CompletableFuture<PostOrderResponse> postOrder(
    @Nonnull String figi,
    long quantity,
    @Nonnull Quotation price,
    @Nonnull OrderDirection direction,
    @Nonnull String accountId,
    @Nonnull OrderType type,
    @Nonnull String orderId) {
    if (readonlyMode) return CompletableFuture.failedFuture(new ReadonlyModeViolationException());

    return Helpers.wrapWithFuture(
      observer -> ordersStub.postOrder(
        PostOrderRequest.newBuilder()
          .setFigi(figi)
          .setQuantity(quantity)
          .setPrice(price)
          .setDirection(direction)
          .setAccountId(accountId)
          .setOrderType(type)
          .setOrderId(Helpers.preprocessInputOrderId(orderId))
          .build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<Instant> cancelOrder(
    @Nonnull String accountId,
    @Nonnull String orderId) {
    if (readonlyMode) return CompletableFuture.failedFuture(new ReadonlyModeViolationException());

    return Helpers.<CancelOrderResponse>wrapWithFuture(
        observer -> ordersStub.cancelOrder(
          CancelOrderRequest.newBuilder()
            .setAccountId(accountId)
            .setOrderId(orderId)
            .build(),
          observer))
      .thenApply(response -> Helpers.timestampToInstant(response.getTime()));
  }

  @Nonnull
  public CompletableFuture<OrderState> getOrderState(
    @Nonnull String accountId,
    @Nonnull String orderId) {
    return Helpers.wrapWithFuture(
      observer -> ordersStub.getOrderState(
        GetOrderStateRequest.newBuilder()
          .setAccountId(accountId)
          .setOrderId(orderId)
          .build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<List<OrderState>> getOrders(@Nonnull String accountId) {
    return Helpers.<GetOrdersResponse>wrapWithFuture(
        observer -> ordersStub.getOrders(
          GetOrdersRequest.newBuilder()
            .setAccountId(accountId)
            .build(),
          observer))
      .thenApply(GetOrdersResponse::getOrdersList);
  }
}
