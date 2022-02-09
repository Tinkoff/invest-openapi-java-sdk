package ru.tinkoff.piapi.core;

import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SandboxServiceTest extends GrpcClientTester<SandboxService> {

  @Override
  protected SandboxService createClient(Channel channel) {
    return new SandboxService(
      SandboxServiceGrpc.newBlockingStub(channel),
      SandboxServiceGrpc.newStub(channel));
  }

  @Test
  void openAccount_Test() {
    var accountId = "accountId";
    var expected = OpenSandboxAccountResponse.newBuilder()
      .setAccountId(accountId)
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void openSandboxAccount(OpenSandboxAccountRequest request,
                                       StreamObserver<OpenSandboxAccountResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.openAccountSync();
    var actualAsync = service.openAccount().join();

    assertEquals(expected.getAccountId(), actualSync);
    assertEquals(expected.getAccountId(), actualAsync);

    var inArg = OpenSandboxAccountRequest.newBuilder().build();
    verify(grpcService, times(2)).openSandboxAccount(eq(inArg), any());
  }

  @Test
  void getAccounts_Test() {
    var expected = GetAccountsResponse.newBuilder()
      .addAccounts(Account.newBuilder().setId("accountId").build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void getSandboxAccounts(GetAccountsRequest request,
                                       StreamObserver<GetAccountsResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getAccountsSync();
    var actualAsync = service.getAccounts().join();

    assertEquals(expected.getAccountsList(), actualSync);
    assertEquals(expected.getAccountsList(), actualAsync);

    var inArg = GetAccountsRequest.newBuilder().build();
    verify(grpcService, times(2)).getSandboxAccounts(eq(inArg), any());
  }

  @Test
  void closeAccount_Test() {
    var accountId = "accountId";
    var expected = CloseSandboxAccountResponse.newBuilder()
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void closeSandboxAccount(CloseSandboxAccountRequest request,
                                        StreamObserver<CloseSandboxAccountResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    service.closeAccountSync(accountId);
    service.closeAccount(accountId).join();

    var inArg = CloseSandboxAccountRequest.newBuilder().setAccountId(accountId).build();
    verify(grpcService, times(2)).closeSandboxAccount(eq(inArg), any());
  }

  @Test
  void postOrder_Test() {
    var expected = PostOrderResponse.newBuilder()
      .setOrderId("orderId")
      .setFigi("figi")
      .setDirection(OrderDirection.ORDER_DIRECTION_BUY)
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void postSandboxOrder(PostOrderRequest request,
                                     StreamObserver<PostOrderResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = PostOrderRequest.newBuilder()
      .setAccountId("accountId")
      .setFigi(expected.getFigi())
      .setDirection(expected.getDirection())
      .setPrice(Quotation.newBuilder().build())
      .build();
    var actualSync = service.postOrderSync(
      inArg.getFigi(), inArg.getQuantity(), inArg.getPrice(), inArg.getDirection(),
      inArg.getAccountId(), inArg.getOrderType(), inArg.getOrderId());
    var actualAsync = service.postOrder(
        inArg.getFigi(), inArg.getQuantity(), inArg.getPrice(), inArg.getDirection(),
        inArg.getAccountId(), inArg.getOrderType(), inArg.getOrderId())
      .join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    verify(grpcService, times(2)).postSandboxOrder(eq(inArg), any());
  }

  @Test
  void getOrders_Test() {
    var accountId = "accountId";
    var expected = GetOrdersResponse.newBuilder()
      .addOrders(OrderState.newBuilder().setOrderId("orderId").build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void getSandboxOrders(GetOrdersRequest request,
                                     StreamObserver<GetOrdersResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getOrdersSync(accountId);
    var actualAsync = service.getOrders(accountId).join();

    assertIterableEquals(expected.getOrdersList(), actualSync);
    assertIterableEquals(expected.getOrdersList(), actualAsync);

    var inArg = GetOrdersRequest.newBuilder()
      .setAccountId(accountId)
      .build();
    verify(grpcService, times(2)).getSandboxOrders(eq(inArg), any());
  }

  @Test
  void cancelOrder_Test() {
    var accountId = "accountId";
    var orderId = "orderId";
    var expected = CancelOrderResponse.newBuilder()
      .setTime(Timestamp.newBuilder().setSeconds(1234567890).setNanos(0).build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void cancelSandboxOrder(CancelOrderRequest request,
                                       StreamObserver<CancelOrderResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.cancelOrderSync(accountId, orderId);
    var actualAsync = service.cancelOrder(accountId, orderId).join();

    assertEquals(DateUtils.timestampToInstant(expected.getTime()), actualSync);
    assertEquals(DateUtils.timestampToInstant(expected.getTime()), actualAsync);

    var inArg = CancelOrderRequest.newBuilder()
      .setAccountId(accountId)
      .setOrderId(orderId)
      .build();
    verify(grpcService, times(2)).cancelSandboxOrder(eq(inArg), any());
  }

  @Test
  void getOrderState_Test() {
    var accountId = "accountId";
    var orderId = "orderId";
    var expected = OrderState.newBuilder()
      .setOrderId(orderId)
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void getSandboxOrderState(GetOrderStateRequest request,
                                         StreamObserver<OrderState> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getOrderStateSync(accountId, orderId);
    var actualAsync = service.getOrderState(accountId, orderId).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = GetOrderStateRequest.newBuilder()
      .setAccountId(accountId)
      .setOrderId(orderId)
      .build();
    verify(grpcService, times(2)).getSandboxOrderState(eq(inArg), any());
  }

  @Test
  void getPositions_Test() {
    var accountId = "accountId";
    var expected = PositionsResponse.newBuilder()
      .setLimitsLoadingInProgress(true)
      .addBlocked(MoneyValue.newBuilder().setUnits(10).build())
      .addMoney(MoneyValue.newBuilder().setUnits(100).build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void getSandboxPositions(PositionsRequest request,
                                        StreamObserver<PositionsResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getPositionsSync(accountId);
    var actualAsync = service.getPositions(accountId).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = PositionsRequest.newBuilder()
      .setAccountId(accountId)
      .build();
    verify(grpcService, times(2)).getSandboxPositions(eq(inArg), any());
  }

  @Test
  void getOperations_Test() {
    var accountId = "accountId";
    var someMoment = Instant.now();
    var expected = OperationsResponse.newBuilder()
      .addOperations(Operation.newBuilder().setId("operationId").build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void getSandboxOperations(OperationsRequest request,
                                         StreamObserver<OperationsResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync =
      service.getOperationsSync(accountId, someMoment, someMoment, OperationState.OPERATION_STATE_CANCELED, null);
    var actualAsync =
      service.getOperations(accountId, someMoment, someMoment, OperationState.OPERATION_STATE_CANCELED, null)
        .join();

    assertEquals(expected.getOperationsList(), actualSync);
    assertEquals(expected.getOperationsList(), actualAsync);

    var inArg = OperationsRequest.newBuilder()
      .setAccountId(accountId)
      .setFrom(DateUtils.instantToTimestamp(someMoment))
      .setTo(DateUtils.instantToTimestamp(someMoment))
      .setState(OperationState.OPERATION_STATE_CANCELED)
      .setFigi("")
      .build();
    verify(grpcService, times(2)).getSandboxOperations(eq(inArg), any());
  }

  @Test
  void getPortfolio_Test() {
    var accountId = "accountId";
    var expected = PortfolioResponse.newBuilder()
      .setTotalAmountBonds(MoneyValue.newBuilder().setUnits(1).build())
      .setTotalAmountCurrencies(MoneyValue.newBuilder().setUnits(2).build())
      .setTotalAmountEtf(MoneyValue.newBuilder().setUnits(3).build())
      .setTotalAmountFutures(MoneyValue.newBuilder().setUnits(4).build())
      .setTotalAmountShares(MoneyValue.newBuilder().setUnits(5).build())
      .setExpectedYield(Quotation.newBuilder().setUnits(6).build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void getSandboxPortfolio(PortfolioRequest request,
                                        StreamObserver<PortfolioResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getPortfolioSync(accountId);
    var actualAsync = service.getPortfolio(accountId).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = PortfolioRequest.newBuilder()
      .setAccountId(accountId)
      .build();
    verify(grpcService, times(2)).getSandboxPortfolio(eq(inArg), any());
  }

  @Test
  void payIn_Test() {
    var accountId = "accountId";
    var expected = SandboxPayInResponse.newBuilder()
      .setBalance(MoneyValue.newBuilder().setCurrency("RUB").setUnits(100).build())
      .build();
    var grpcService = mock(SandboxServiceGrpc.SandboxServiceImplBase.class, delegatesTo(
      new SandboxServiceGrpc.SandboxServiceImplBase() {
        @Override
        public void sandboxPayIn(SandboxPayInRequest request,
                                 StreamObserver<SandboxPayInResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = SandboxPayInRequest.newBuilder()
      .setAccountId(accountId)
      .setAmount(MoneyValue.newBuilder().setCurrency("RUB").setUnits(100).build())
      .build();
    var actualSync = service.payInSync(inArg.getAccountId(), inArg.getAmount());
    var actualAsync = service.payIn(inArg.getAccountId(), inArg.getAmount()).join();

    assertEquals(expected.getBalance(), actualSync);
    assertEquals(expected.getBalance(), actualAsync);

    verify(grpcService, times(2)).sandboxPayIn(eq(inArg), any());
  }

}
