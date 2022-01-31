package ru.tinkoff.piapi.core;

import ru.tinkoff.piapi.contract.v1.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OperationsService {
  private final OperationsServiceGrpc.OperationsServiceBlockingStub operationsBlockingStub;
  private final OperationsServiceGrpc.OperationsServiceStub operationsStub;

  OperationsService(
    @Nonnull OperationsServiceGrpc.OperationsServiceBlockingStub operationsBlockingStub,
    @Nonnull OperationsServiceGrpc.OperationsServiceStub operationsStub) {
    this.operationsBlockingStub = operationsBlockingStub;
    this.operationsStub = operationsStub;
  }

  @Nonnull
  public List<Operation> getOperationsSync(
    @Nonnull String accountId,
    @Nonnull Instant from,
    @Nonnull Instant to,
    @Nonnull OperationState operationState,
    @Nullable String figi) {
    return operationsBlockingStub.getOperations(
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
    return operationsBlockingStub.getPortfolio(
      PortfolioRequest.newBuilder().setAccountId(accountId).build());
  }

  @Nonnull
  public PositionsResponse getPositionsSync(@Nonnull String accountId) {
    return operationsBlockingStub.getPositions(
      PositionsRequest.newBuilder().setAccountId(accountId).build());
  }

  @Nonnull
  public WithdrawLimitsResponse getWithdrawLimitsSync(@Nonnull String accountId) {
    return operationsBlockingStub.getWithdrawLimits(
      WithdrawLimitsRequest.newBuilder().setAccountId(accountId).build());
  }

  @Nonnull
  public CompletableFuture<List<Operation>> getOperations(
    @Nonnull String accountId,
    @Nonnull Instant from,
    @Nonnull Instant to,
    @Nonnull OperationState operationState,
    @Nullable String figi) {
    return Helpers.<OperationsResponse>wrapWithFuture(
        observer -> operationsStub.getOperations(
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
      observer -> operationsStub.getPortfolio(
        PortfolioRequest.newBuilder().setAccountId(accountId).build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<PositionsResponse> getPositions(@Nonnull String accountId) {
    return Helpers.wrapWithFuture(
      observer -> operationsStub.getPositions(
        PositionsRequest.newBuilder().setAccountId(accountId).build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<WithdrawLimitsResponse> getWithdrawLimits(
    @Nonnull String accountId) {
    return Helpers.wrapWithFuture(
      observer -> operationsStub.getWithdrawLimits(
        WithdrawLimitsRequest.newBuilder().setAccountId(accountId).build(),
        observer));
  }
}
