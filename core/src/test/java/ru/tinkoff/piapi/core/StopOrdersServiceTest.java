package ru.tinkoff.piapi.core;

import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import ru.tinkoff.piapi.contract.v1.*;

import java.time.Instant;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class StopOrdersServiceTest extends GrpcClientTester<StopOrdersService> {

  @Rule
  public ExpectedException futureThrown = ExpectedException.none();

  @Override
  protected StopOrdersService createClient(Channel channel) {
    return new StopOrdersService(
      StopOrdersServiceGrpc.newBlockingStub(channel),
      StopOrdersServiceGrpc.newStub(channel),
      false);
  }

  private StopOrdersService createReadonlyClient(Channel channel) {
    return new StopOrdersService(
      StopOrdersServiceGrpc.newBlockingStub(channel),
      StopOrdersServiceGrpc.newStub(channel),
      true);
  }

  @Test
  void postStopOrderGoodTillCancel_Test() {
    var expected = PostStopOrderResponse.newBuilder()
      .setStopOrderId("stopOrderId")
      .build();
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class, delegatesTo(
      new StopOrdersServiceGrpc.StopOrdersServiceImplBase() {
        @Override
        public void postStopOrder(PostStopOrderRequest request,
                                  StreamObserver<PostStopOrderResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = PostStopOrderRequest.newBuilder()
      .setFigi("figi")
      .setQuantity(1)
      .setPrice(Quotation.newBuilder().setUnits(100).setNano(0).build())
      .setStopPrice(Quotation.newBuilder().setUnits(110).setNano(0).build())
      .setDirection(StopOrderDirection.STOP_ORDER_DIRECTION_SELL)
      .setAccountId("accountId")
      .setExpirationType(StopOrderExpirationType.STOP_ORDER_EXPIRATION_TYPE_GOOD_TILL_CANCEL)
      .setStopOrderType(StopOrderType.STOP_ORDER_TYPE_TAKE_PROFIT)
      .build();
    var actualSync = service.postStopOrderGoodTillCancelSync(
      inArg.getFigi(),
      inArg.getQuantity(),
      inArg.getPrice(),
      inArg.getStopPrice(),
      inArg.getDirection(),
      inArg.getAccountId(),
      inArg.getStopOrderType()
    );
    var actualAsync = service.postStopOrderGoodTillCancel(
      inArg.getFigi(),
      inArg.getQuantity(),
      inArg.getPrice(),
      inArg.getStopPrice(),
      inArg.getDirection(),
      inArg.getAccountId(),
      inArg.getStopOrderType()
    ).join();

    assertEquals(expected.getStopOrderId(), actualSync);
    assertEquals(expected.getStopOrderId(), actualAsync);

    verify(grpcService, times(2)).postStopOrder(eq(inArg), any());
  }

  @Test
  void postStopOrderGoodTillDate_Test() {
    var expected = PostStopOrderResponse.newBuilder()
      .setStopOrderId("stopOrderId")
      .build();
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class, delegatesTo(
      new StopOrdersServiceGrpc.StopOrdersServiceImplBase() {
        @Override
        public void postStopOrder(PostStopOrderRequest request,
                                  StreamObserver<PostStopOrderResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = PostStopOrderRequest.newBuilder()
      .setFigi("figi")
      .setQuantity(1)
      .setPrice(Quotation.newBuilder().setUnits(100).setNano(0).build())
      .setStopPrice(Quotation.newBuilder().setUnits(110).setNano(0).build())
      .setDirection(StopOrderDirection.STOP_ORDER_DIRECTION_SELL)
      .setAccountId("accountId")
      .setExpirationType(StopOrderExpirationType.STOP_ORDER_EXPIRATION_TYPE_GOOD_TILL_DATE)
      .setStopOrderType(StopOrderType.STOP_ORDER_TYPE_TAKE_PROFIT)
      .setExpireDate(Timestamp.newBuilder().setSeconds(1234567890).setNanos(0).build())
      .build();
    var actualSync = service.postStopOrderGoodTillDateSync(
      inArg.getFigi(),
      inArg.getQuantity(),
      inArg.getPrice(),
      inArg.getStopPrice(),
      inArg.getDirection(),
      inArg.getAccountId(),
      inArg.getStopOrderType(),
      Helpers.timestampToInstant(inArg.getExpireDate())
    );
    var actualAsync = service.postStopOrderGoodTillDate(
      inArg.getFigi(),
      inArg.getQuantity(),
      inArg.getPrice(),
      inArg.getStopPrice(),
      inArg.getDirection(),
      inArg.getAccountId(),
      inArg.getStopOrderType(),
      Helpers.timestampToInstant(inArg.getExpireDate())
    ).join();

    assertEquals(expected.getStopOrderId(), actualSync);
    assertEquals(expected.getStopOrderId(), actualAsync);

    verify(grpcService, times(2)).postStopOrder(eq(inArg), any());
  }

  @Test
  void getStopOrders_Test() {
    var accountId = "accountId";
    var expected = GetStopOrdersResponse.newBuilder()
      .addStopOrders(StopOrder.newBuilder()
        .setStopOrderId("stopOrderId")
        .setFigi("figi")
        .build())
      .build();
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class, delegatesTo(
      new StopOrdersServiceGrpc.StopOrdersServiceImplBase() {
        @Override
        public void getStopOrders(GetStopOrdersRequest request,
                                  StreamObserver<GetStopOrdersResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.getStopOrdersSync(accountId);
    var actualAsync = service.getStopOrders(accountId).join();

    assertIterableEquals(expected.getStopOrdersList(), actualSync);
    assertIterableEquals(expected.getStopOrdersList(), actualAsync);

    var inArg = GetStopOrdersRequest.newBuilder()
      .setAccountId(accountId)
      .build();
    verify(grpcService, times(2)).getStopOrders(eq(inArg), any());
  }

  @Test
  void cancelStopOrder_Test() {
    var accountId = "accountId";
    var stopOrderId = "stopOrderId";
    var expected = CancelStopOrderResponse.newBuilder()
      .setTime(Timestamp.newBuilder()
        .setSeconds(1234567890)
        .setNanos(0)
        .build())
      .build();
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class, delegatesTo(
      new StopOrdersServiceGrpc.StopOrdersServiceImplBase() {
        @Override
        public void cancelStopOrder(CancelStopOrderRequest request,
                                    StreamObserver<CancelStopOrderResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.cancelStopOrderSync(accountId, stopOrderId);
    var actualAsync = service.cancelStopOrder(accountId, stopOrderId).join();

    assertEquals(Helpers.timestampToInstant(expected.getTime()), actualSync);
    assertEquals(Helpers.timestampToInstant(expected.getTime()), actualAsync);

    var inArg = CancelStopOrderRequest.newBuilder()
      .setAccountId(accountId)
      .setStopOrderId(stopOrderId)
      .build();
    verify(grpcService, times(2)).cancelStopOrder(eq(inArg), any());
  }

  @Test
  void postStopOrderGoodTillCancel_forbiddenInReadonly_Test() {
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class);
    var readonlyService = mkClientBasedOnServer(grpcService, this::createReadonlyClient);

    assertThrows(
      ReadonlyModeViolationException.class,
      () -> readonlyService.postStopOrderGoodTillCancelSync(
        "", 0, Quotation.getDefaultInstance(), Quotation.getDefaultInstance(),
        StopOrderDirection.STOP_ORDER_DIRECTION_UNSPECIFIED, "", StopOrderType.STOP_ORDER_TYPE_UNSPECIFIED)
    );
    futureThrown.expect(CompletionException.class);
    futureThrown.expectCause(IsInstanceOf.instanceOf(ReadonlyModeViolationException.class));
    readonlyService.postStopOrderGoodTillCancel(
        "", 0, Quotation.getDefaultInstance(), Quotation.getDefaultInstance(),
        StopOrderDirection.STOP_ORDER_DIRECTION_UNSPECIFIED, "", StopOrderType.STOP_ORDER_TYPE_UNSPECIFIED);
  }

  @Test
  void postStopOrderGoodTillDate_forbiddenInReadonly_Test() {
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class);
    var readonlyService = mkClientBasedOnServer(grpcService, this::createReadonlyClient);

    assertThrows(
      ReadonlyModeViolationException.class,
      () -> readonlyService.postStopOrderGoodTillDateSync(
        "", 0, Quotation.getDefaultInstance(), Quotation.getDefaultInstance(),
        StopOrderDirection.STOP_ORDER_DIRECTION_UNSPECIFIED, "", StopOrderType.STOP_ORDER_TYPE_UNSPECIFIED,
        Instant.EPOCH));
    futureThrown.expect(CompletionException.class);
    futureThrown.expectCause(IsInstanceOf.instanceOf(ReadonlyModeViolationException.class));
    readonlyService.postStopOrderGoodTillDate(
        "", 0, Quotation.getDefaultInstance(), Quotation.getDefaultInstance(),
        StopOrderDirection.STOP_ORDER_DIRECTION_UNSPECIFIED, "", StopOrderType.STOP_ORDER_TYPE_UNSPECIFIED,
        Instant.EPOCH);
  }

  @Test
  void cancelStopOrder_forbiddenInReadonly_Test() {
    var grpcService = mock(StopOrdersServiceGrpc.StopOrdersServiceImplBase.class);
    var readonlyService = mkClientBasedOnServer(grpcService, this::createReadonlyClient);

    assertThrows(
      ReadonlyModeViolationException.class,
      () -> readonlyService.cancelStopOrderSync("", ""));
    futureThrown.expect(CompletionException.class);
    futureThrown.expectCause(IsInstanceOf.instanceOf(ReadonlyModeViolationException.class));
    readonlyService.cancelStopOrder("", "");
  }
}
