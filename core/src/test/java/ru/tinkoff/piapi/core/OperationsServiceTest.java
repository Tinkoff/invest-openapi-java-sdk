package ru.tinkoff.piapi.core;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OperationsServiceTest extends GrpcClientTester<OperationsService> {

  @Override
  protected OperationsService createClient(Channel channel) {
    return new OperationsService(
      OperationsServiceGrpc.newBlockingStub(channel),
      OperationsServiceGrpc.newStub(channel));
  }

  @Test
  void getPositions_Test() {
    var accountId = "accountId";
    var expected = PositionsResponse.newBuilder()
      .setLimitsLoadingInProgress(true)
      .addBlocked(MoneyValue.newBuilder().setUnits(10).build())
      .addMoney(MoneyValue.newBuilder().setUnits(100).build())
      .build();
    var grpcService = mock(OperationsServiceGrpc.OperationsServiceImplBase.class, delegatesTo(
      new OperationsServiceGrpc.OperationsServiceImplBase() {
        @Override
        public void getPositions(PositionsRequest request,
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
    verify(grpcService, times(2)).getPositions(eq(inArg), any());
  }

  @Test
  void getOperations_Test() {
    var accountId = "accountId";
    var someMoment = Instant.now();
    var expected = OperationsResponse.newBuilder()
      .addOperations(Operation.newBuilder().setId("operationId").build())
      .build();
    var grpcService = mock(OperationsServiceGrpc.OperationsServiceImplBase.class, delegatesTo(
      new OperationsServiceGrpc.OperationsServiceImplBase() {
        @Override
        public void getOperations(OperationsRequest request,
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
      .setFrom(Helpers.instantToTimestamp(someMoment))
      .setTo(Helpers.instantToTimestamp(someMoment))
      .setState(OperationState.OPERATION_STATE_CANCELED)
      .setFigi("")
      .build();
    verify(grpcService, times(2)).getOperations(eq(inArg), any());
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
      .setExpectedYield(1.0f)
      .build();
    var grpcService = mock(OperationsServiceGrpc.OperationsServiceImplBase.class, delegatesTo(
      new OperationsServiceGrpc.OperationsServiceImplBase() {
        @Override
        public void getPortfolio(PortfolioRequest request,
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
    verify(grpcService, times(2)).getPortfolio(eq(inArg), any());
  }

  @Test
  void getWithdrawLimits_Test() {
    var accountId = "accountId";
    var expected = WithdrawLimitsResponse.newBuilder()
      .addBlocked(MoneyValue.newBuilder().setUnits(1).build())
      .addMoney(MoneyValue.newBuilder().setUnits(2).build())
      .build();
    var grpcService = mock(OperationsServiceGrpc.OperationsServiceImplBase.class, delegatesTo(
      new OperationsServiceGrpc.OperationsServiceImplBase() {
        @Override
        public void getWithdrawLimits(WithdrawLimitsRequest request,
                                      StreamObserver<WithdrawLimitsResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getWithdrawLimitsSync(accountId);
    var actualAsync = service.getWithdrawLimits(accountId).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    var inArg = WithdrawLimitsRequest.newBuilder()
      .setAccountId(accountId)
      .build();
    verify(grpcService, times(2)).getWithdrawLimits(eq(inArg), any());
  }

}
