package ru.tinkoff.piapi.core;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.Mockito.*;

public class UsersServiceTest extends GrpcClientTester<UsersService> {

  static MoneyValue someMoney = MoneyValue.newBuilder().setCurrency("RUB").setUnits(1).setNano(2).build();
  static Quotation someQuote = Quotation.newBuilder().setUnits(1).setNano(2).build();

  @Override
  protected UsersService createClient(Channel channel) {
    return new UsersService(UsersServiceGrpc.newBlockingStub(channel), UsersServiceGrpc.newStub(channel));
  }

  @Test
  void getAccounts_Test() {
    var expected = List.of(
      Account.newBuilder().setId("1").build(),
      Account.newBuilder().setId("2").build());
    var grpcService = mock(UsersServiceGrpc.UsersServiceImplBase.class, delegatesTo(
      new UsersServiceGrpc.UsersServiceImplBase() {
        @Override
        public void getAccounts(GetAccountsRequest request,
                                StreamObserver<GetAccountsResponse> responseObserver) {
          responseObserver.onNext(GetAccountsResponse.newBuilder().addAllAccounts(expected).build());
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getAccountsSync();
    var actualAsync = service.getAccounts().join();

    assertIterableEquals(expected, actualSync);
    assertIterableEquals(expected, actualAsync);

    var inArg = GetAccountsRequest.newBuilder().build();
    verify(grpcService, times(2)).getAccounts(eq(inArg), any());
  }

  @Test
  void getMarginAttributes_Test() {
    var accountId = "accountId";
    var expected = GetMarginAttributesResponse.newBuilder()
      .setAmountOfMissingFunds(someMoney)
      .setFundsSufficiencyLevel(someQuote)
      .setMinimalMargin(someMoney)
      .setStartingMargin(someMoney)
      .setLiquidPortfolio(someMoney)
      .build();
    var grpcService = mock(UsersServiceGrpc.UsersServiceImplBase.class, delegatesTo(
      new UsersServiceGrpc.UsersServiceImplBase() {
        @Override
        public void getMarginAttributes(GetMarginAttributesRequest request,
                                        StreamObserver<GetMarginAttributesResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getMarginAttributesSync(accountId);
    var actualAsync = service.getMarginAttributes(accountId).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = GetMarginAttributesRequest.newBuilder().setAccountId(accountId).build();
    verify(grpcService, times(2)).getMarginAttributes(eq(inArg), any());
  }

  @Test
  void getUserTariff_Test() {
    var expected = GetUserTariffResponse.newBuilder()
      .addUnaryLimits(UnaryLimit.newBuilder().setLimitPerMinute(1).addMethods("method").build())
      .addStreamLimits(StreamLimit.newBuilder().setLimit(1).addStreams("stream").build())
      .build();
    var grpcService = mock(UsersServiceGrpc.UsersServiceImplBase.class, delegatesTo(
      new UsersServiceGrpc.UsersServiceImplBase() {
        @Override
        public void getUserTariff(GetUserTariffRequest request,
                                  StreamObserver<GetUserTariffResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getUserTariffSync();
    var actualAsync = service.getUserTariff().join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = GetUserTariffRequest.newBuilder().build();
    verify(grpcService, times(2)).getUserTariff(eq(inArg), any());
  }

  @Test
  void getInfo_Test() {
    var expected = GetInfoResponse.newBuilder()
      .setPremStatus(true)
      .setQualStatus(true)
      .addQualifiedForWorkWith("instrument")
      .build();
    var grpcService = mock(UsersServiceGrpc.UsersServiceImplBase.class, delegatesTo(
      new UsersServiceGrpc.UsersServiceImplBase() {
        @Override
        public void getInfo(GetInfoRequest request,
                            StreamObserver<GetInfoResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getInfoSync();
    var actualAsync = service.getInfo().join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = GetInfoRequest.newBuilder().build();
    verify(grpcService, times(2)).getInfo(eq(inArg), any());
  }

}
