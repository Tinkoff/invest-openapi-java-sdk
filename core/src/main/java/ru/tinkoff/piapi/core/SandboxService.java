package ru.tinkoff.piapi.core;

import ru.tinkoff.piapi.contract.v1.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SandboxService {
  private final SandboxServiceGrpc.SandboxServiceBlockingStub sandboxBlockingStub;
  private final SandboxServiceGrpc.SandboxServiceStub sandboxStub;

  SandboxService(
    @Nonnull SandboxServiceGrpc.SandboxServiceBlockingStub sandboxBlockingStub,
    @Nonnull SandboxServiceGrpc.SandboxServiceStub sandboxStub) {
    this.sandboxBlockingStub = sandboxBlockingStub;
    this.sandboxStub = sandboxStub;
  }

  @Nonnull
  public String openAccountSync() {
    return sandboxBlockingStub.openSandboxAccount(
        OpenSandboxAccountRequest.newBuilder()
          .build())
      .getAccountId();
  }

  @Nonnull
  public List<Account> getAccountsSync() {
    return sandboxBlockingStub.getSandboxAccounts(
        GetAccountsRequest.newBuilder()
          .build())
      .getAccountsList();
  }

  public void closeAccountSync(@Nonnull String accountId) {
    //noinspection ResultOfMethodCallIgnored
    sandboxBlockingStub.closeSandboxAccount(
      CloseSandboxAccountRequest.newBuilder()
        .setAccountId(accountId)
        .build());
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
    return sandboxBlockingStub.postSandboxOrder(
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
  public List<OrderState> getOrdersSync(@Nonnull String accountId) {
    return sandboxBlockingStub.getSandboxOrders(
        GetOrdersRequest.newBuilder()
          .setAccountId(accountId)
          .build())
      .getOrdersList();
  }

  @Nonnull
  public Instant cancelOrderSync(
    @Nonnull String accountId,
    @Nonnull String orderId) {
    var responseTime = sandboxBlockingStub.cancelSandboxOrder(
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
    return sandboxBlockingStub.getSandboxOrderState(
      GetOrderStateRequest.newBuilder()
        .setAccountId(accountId)
        .setOrderId(orderId)
        .build());
  }

  @Nonnull
  public PositionsResponse getPositionsSync(@Nonnull String accountId) {
    return sandboxBlockingStub.getSandboxPositions(
      PositionsRequest.newBuilder().setAccountId(accountId).build());
  }

  @Nonnull
  public List<Operation> getOperationsSync(
    @Nonnull String accountId,
    @Nonnull Instant from,
    @Nonnull Instant to,
    @Nonnull OperationState operationState,
    @Nullable String figi) {
    return sandboxBlockingStub.getSandboxOperations(
      OperationsRequest.newBuilder()
        .setAccountId(accountId)
        .setFrom(Helpers.instantToTimestamp(from))
        .setTo(Helpers.instantToTimestamp(to))
        .setState(operationState)
        .setFigi(figi == null ? "" : figi)
        .build())
      .getOperationsList();
  }

  @Nonnull
  public PortfolioResponse getPortfolioSync(@Nonnull String accountId) {
    return sandboxBlockingStub.getSandboxPortfolio(
      PortfolioRequest.newBuilder().setAccountId(accountId).build());
  }

  @Nonnull
  public MoneyValue payInSync(@Nonnull String accountId, @Nonnull MoneyValue moneyValue) {
    return sandboxBlockingStub.sandboxPayIn(
        SandboxPayInRequest.newBuilder()
          .setAccountId(accountId)
          .setAmount(moneyValue)
          .build())
      .getBalance();
  }

  @Nonnull
  public CompletableFuture<String> openAccount() {
    return Helpers.<OpenSandboxAccountResponse>wrapWithFuture(
        observer -> sandboxStub.openSandboxAccount(
          OpenSandboxAccountRequest.newBuilder()
            .build(),
          observer))
      .thenApply(OpenSandboxAccountResponse::getAccountId);
  }

  @Nonnull
  public CompletableFuture<List<Account>> getAccounts() {
    return Helpers.<GetAccountsResponse>wrapWithFuture(
        observer -> sandboxStub.getSandboxAccounts(
          GetAccountsRequest.newBuilder().build(),
          observer))
      .thenApply(GetAccountsResponse::getAccountsList);
  }

  @Nonnull
  public CompletableFuture<Void> closeAccount(@Nonnull String accountId) {
    return Helpers.<CloseSandboxAccountResponse>wrapWithFuture(
        observer -> sandboxStub.closeSandboxAccount(
          CloseSandboxAccountRequest.newBuilder()
            .setAccountId(accountId)
            .build(),
          observer))
      .thenApply(r -> null);
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
    return Helpers.wrapWithFuture(
      observer -> sandboxStub.postSandboxOrder(
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
  public CompletableFuture<List<OrderState>> getOrders(@Nonnull String accountId) {
    return Helpers.<GetOrdersResponse>wrapWithFuture(
        observer -> sandboxStub.getSandboxOrders(
          GetOrdersRequest.newBuilder()
            .setAccountId(accountId)
            .build(),
          observer))
      .thenApply(GetOrdersResponse::getOrdersList);
  }

  @Nonnull
  public CompletableFuture<Instant> cancelOrder(
    @Nonnull String accountId,
    @Nonnull String orderId) {
    return Helpers.<CancelOrderResponse>wrapWithFuture(
        observer -> sandboxStub.cancelSandboxOrder(
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
      observer -> sandboxStub.getSandboxOrderState(
        GetOrderStateRequest.newBuilder()
          .setAccountId(accountId)
          .setOrderId(orderId)
          .build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<PositionsResponse> getPositions(@Nonnull String accountId) {
    return Helpers.wrapWithFuture(
      observer -> sandboxStub.getSandboxPositions(
        PositionsRequest.newBuilder().setAccountId(accountId).build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<List<Operation>> getOperations(
    @Nonnull String accountId,
    @Nonnull Instant from,
    @Nonnull Instant to,
    @Nonnull OperationState operationState,
    @Nullable String figi) {
    return Helpers.<OperationsResponse>wrapWithFuture(
      observer -> sandboxStub.getSandboxOperations(
        OperationsRequest.newBuilder()
          .setAccountId(accountId)
          .setFrom(Helpers.instantToTimestamp(from))
          .setTo(Helpers.instantToTimestamp(to))
          .setState(operationState)
          .setFigi(figi == null ? "" : figi)
          .build(),
        observer))
      .thenApply(OperationsResponse::getOperationsList);
  }

  @Nonnull
  public CompletableFuture<PortfolioResponse> getPortfolio(@Nonnull String accountId) {
    return Helpers.wrapWithFuture(
      observer -> sandboxStub.getSandboxPortfolio(
        PortfolioRequest.newBuilder().setAccountId(accountId).build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<MoneyValue> payIn(
    @Nonnull String accountId,
    @Nonnull MoneyValue moneyValue) {
    return Helpers.<SandboxPayInResponse>wrapWithFuture(
        observer -> sandboxStub.sandboxPayIn(
          SandboxPayInRequest.newBuilder()
            .setAccountId(accountId)
            .setAmount(moneyValue)
            .build(),
          observer))
      .thenApply(SandboxPayInResponse::getBalance);
  }

}
