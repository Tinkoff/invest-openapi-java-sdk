package ru.tinkoff.piapi.core;

import ru.tinkoff.piapi.contract.v1.*;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StopOrdersService {
  private final StopOrdersServiceGrpc.StopOrdersServiceBlockingStub stopOrdersBlockingStub;
  private final StopOrdersServiceGrpc.StopOrdersServiceStub stopOrdersStub;
  private final boolean readonlyMode;

  StopOrdersService(
    @Nonnull StopOrdersServiceGrpc.StopOrdersServiceBlockingStub stopOrdersBlockingStub,
    @Nonnull StopOrdersServiceGrpc.StopOrdersServiceStub stopOrdersStub,
    boolean readonlyMode) {
    this.stopOrdersBlockingStub = stopOrdersBlockingStub;
    this.stopOrdersStub = stopOrdersStub;
    this.readonlyMode = readonlyMode;
  }

  @Nonnull
  public String postStopOrderGoodTillCancelSync(
    @Nonnull String figi,
    long quantity,
    @Nonnull Quotation price,
    @Nonnull Quotation stopPrice,
    @Nonnull StopOrderDirection direction,
    @Nonnull String accountId,
    @Nonnull StopOrderType type) {
    if (readonlyMode) throw new ReadonlyModeViolationException();

    return stopOrdersBlockingStub.postStopOrder(
        PostStopOrderRequest.newBuilder()
          .setFigi(figi)
          .setQuantity(quantity)
          .setPrice(price)
          .setStopPrice(stopPrice)
          .setDirection(direction)
          .setAccountId(accountId)
          .setExpirationType(StopOrderExpirationType.STOP_ORDER_EXPIRATION_TYPE_GOOD_TILL_CANCEL)
          .setStopOrderType(type)
          .build())
      .getStopOrderId();
  }

  @Nonnull
  public String postStopOrderGoodTillDateSync(
    @Nonnull String figi,
    long quantity,
    @Nonnull Quotation price,
    @Nonnull Quotation stopPrice,
    @Nonnull StopOrderDirection direction,
    @Nonnull String accountId,
    @Nonnull StopOrderType type,
    @Nonnull Instant expireDate) {
    if (readonlyMode) throw new ReadonlyModeViolationException();

    return stopOrdersBlockingStub.postStopOrder(
        PostStopOrderRequest.newBuilder()
          .setFigi(figi)
          .setQuantity(quantity)
          .setPrice(price)
          .setStopPrice(stopPrice)
          .setDirection(direction)
          .setAccountId(accountId)
          .setExpirationType(StopOrderExpirationType.STOP_ORDER_EXPIRATION_TYPE_GOOD_TILL_DATE)
          .setStopOrderType(type)
          .setExpireDate(Helpers.instantToTimestamp(expireDate))
          .build())
      .getStopOrderId();
  }

  @Nonnull
  public List<StopOrder> getStopOrdersSync(@Nonnull String accountId) {
    return stopOrdersBlockingStub.getStopOrders(
        GetStopOrdersRequest.newBuilder()
          .setAccountId(accountId)
          .build())
      .getStopOrdersList();
  }

  @Nonnull
  public Instant cancelStopOrderSync(
    @Nonnull String accountId,
    @Nonnull String stopOrderId) {
    if (readonlyMode) throw new ReadonlyModeViolationException();

    var responseTime = stopOrdersBlockingStub.cancelStopOrder(
        CancelStopOrderRequest.newBuilder()
          .setAccountId(accountId)
          .setStopOrderId(stopOrderId)
          .build())
      .getTime();

    return Helpers.timestampToInstant(responseTime);
  }

  @Nonnull
  public CompletableFuture<String> postStopOrderGoodTillCancel(
    @Nonnull String figi,
    long quantity,
    @Nonnull Quotation price,
    @Nonnull Quotation stopPrice,
    @Nonnull StopOrderDirection direction,
    @Nonnull String accountId,
    @Nonnull StopOrderType type) {
    if (readonlyMode) return CompletableFuture.failedFuture(new ReadonlyModeViolationException());

    return Helpers.<PostStopOrderResponse>wrapWithFuture(
        observer -> stopOrdersStub.postStopOrder(
          PostStopOrderRequest.newBuilder()
            .setFigi(figi)
            .setQuantity(quantity)
            .setPrice(price)
            .setStopPrice(stopPrice)
            .setDirection(direction)
            .setAccountId(accountId)
            .setExpirationType(StopOrderExpirationType.STOP_ORDER_EXPIRATION_TYPE_GOOD_TILL_CANCEL)
            .setStopOrderType(type)
            .build(),
          observer))
      .thenApply(PostStopOrderResponse::getStopOrderId);
  }

  @Nonnull
  public CompletableFuture<String> postStopOrderGoodTillDate(
    @Nonnull String figi,
    long quantity,
    @Nonnull Quotation price,
    @Nonnull Quotation stopPrice,
    @Nonnull StopOrderDirection direction,
    @Nonnull String accountId,
    @Nonnull StopOrderType type,
    @Nonnull Instant expireDate) {
    if (readonlyMode) return CompletableFuture.failedFuture(new ReadonlyModeViolationException());

    return Helpers.<PostStopOrderResponse>wrapWithFuture(
        observer -> stopOrdersStub.postStopOrder(
          PostStopOrderRequest.newBuilder()
            .setFigi(figi)
            .setQuantity(quantity)
            .setPrice(price)
            .setStopPrice(stopPrice)
            .setDirection(direction)
            .setAccountId(accountId)
            .setExpirationType(StopOrderExpirationType.STOP_ORDER_EXPIRATION_TYPE_GOOD_TILL_DATE)
            .setStopOrderType(type)
            .setExpireDate(Helpers.instantToTimestamp(expireDate))
            .build(),
          observer))
      .thenApply(PostStopOrderResponse::getStopOrderId);
  }

  @Nonnull
  public CompletableFuture<List<StopOrder>> getStopOrders(@Nonnull String accountId) {
    return Helpers.<GetStopOrdersResponse>wrapWithFuture(
        observer -> stopOrdersStub.getStopOrders(
          GetStopOrdersRequest.newBuilder()
            .setAccountId(accountId)
            .build(),
          observer))
      .thenApply(GetStopOrdersResponse::getStopOrdersList);
  }

  @Nonnull
  public CompletableFuture<Instant> cancelStopOrder(
    @Nonnull String accountId,
    @Nonnull String stopOrderId) {
    if (readonlyMode) return CompletableFuture.failedFuture(new ReadonlyModeViolationException());

    return Helpers.<CancelStopOrderResponse>wrapWithFuture(
        observer -> stopOrdersStub.cancelStopOrder(
          CancelStopOrderRequest.newBuilder()
            .setAccountId(accountId)
            .setStopOrderId(stopOrderId)
            .build(),
          observer))
      .thenApply(response -> Helpers.timestampToInstant(response.getTime()));
  }
}
