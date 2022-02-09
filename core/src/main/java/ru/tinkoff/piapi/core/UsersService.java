package ru.tinkoff.piapi.core;

import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UsersService {
  private final UsersServiceGrpc.UsersServiceBlockingStub userBlockingStub;
  private final UsersServiceGrpc.UsersServiceStub userStub;

  UsersService(
    @Nonnull UsersServiceGrpc.UsersServiceBlockingStub userBlockingStub,
    @Nonnull UsersServiceGrpc.UsersServiceStub userStub) {
    this.userBlockingStub = userBlockingStub;
    this.userStub = userStub;
  }

  @Nonnull
  public List<Account> getAccountsSync() {
    return userBlockingStub.getAccounts(
        GetAccountsRequest.newBuilder()
          .build())
      .getAccountsList();
  }

  @Nonnull
  public GetMarginAttributesResponse getMarginAttributesSync(@Nonnull String accountId) {
    return userBlockingStub.getMarginAttributes(
      GetMarginAttributesRequest.newBuilder().setAccountId(accountId).build());
  }

  @Nonnull
  public GetUserTariffResponse getUserTariffSync() {
    return userBlockingStub.getUserTariff(GetUserTariffRequest.newBuilder().build());
  }

  @Nonnull
  public GetInfoResponse getInfoSync() {
    return userBlockingStub.getInfo(GetInfoRequest.newBuilder().build());
  }

  @Nonnull
  public CompletableFuture<List<Account>> getAccounts() {
    return Helpers.<GetAccountsResponse>wrapWithFuture(
        observer -> userStub.getAccounts(
          GetAccountsRequest.newBuilder().build(),
          observer))
      .thenApply(GetAccountsResponse::getAccountsList);
  }

  @Nonnull
  public CompletableFuture<GetMarginAttributesResponse> getMarginAttributes(
    @Nonnull String accountId) {
    return Helpers.wrapWithFuture(
      observer -> userStub.getMarginAttributes(
        GetMarginAttributesRequest.newBuilder().setAccountId(accountId).build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<GetUserTariffResponse> getUserTariff() {
    return Helpers.wrapWithFuture(
      observer -> userStub.getUserTariff(
        GetUserTariffRequest.newBuilder().build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<GetInfoResponse> getInfo() {
    return Helpers.wrapWithFuture(
      observer -> userStub.getInfo(
        GetInfoRequest.newBuilder().build(),
        observer));
  }
}
