package ru.tinkoff.piapi.core;

import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.smallrye.mutiny.Multi;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.reactivestreams.FlowAdapters;
import ru.tinkoff.piapi.contract.v1.*;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OrdersServiceTest extends GrpcClientTester<OrdersService> {

  @Rule
  public ExpectedException futureThrown = ExpectedException.none();

  @Override
  protected OrdersService createClient(Channel channel) {
    return new OrdersService(
      OrdersStreamServiceGrpc.newStub(channel),
      OrdersServiceGrpc.newBlockingStub(channel),
      OrdersServiceGrpc.newStub(channel),
      false);
  }

  @Test
  void tradeStream_Test() {
    var expected = List.of(
      TradesStreamResponse.newBuilder().setOrderTrades(OrderTrades.newBuilder().setOrderId("order1").build()).build(),
      TradesStreamResponse.newBuilder().setOrderTrades(OrderTrades.newBuilder().setOrderId("order2").build()).build(),
      TradesStreamResponse.newBuilder().setOrderTrades(OrderTrades.newBuilder().setOrderId("order3").build()).build()
    );
    var grpcService = mock(OrdersStreamServiceGrpc.OrdersStreamServiceImplBase.class, delegatesTo(
      new OrdersStreamServiceGrpc.OrdersStreamServiceImplBase() {
        @Override
        public void tradesStream(TradesStreamRequest request,
                                 StreamObserver<TradesStreamResponse> responseObserver) {
          for (var item : expected) {
            responseObserver.onNext(item);
          }
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actual = Multi.createFrom()
      .publisher(FlowAdapters.toPublisher(service.tradesStream()))
      .subscribe()
      .asStream()
      .collect(Collectors.toList());

    assertIterableEquals(expected, actual);

    var inArg = TradesStreamRequest.newBuilder()
      .build();
    verify(grpcService).tradesStream(eq(inArg), any());
  }

  @Test
  void postOrder_Test() {
    var expected = PostOrderResponse.newBuilder()
      .setOrderId("orderId")
      .setFigi("figi")
      .setDirection(OrderDirection.ORDER_DIRECTION_BUY)
      .build();
    var grpcService = mock(OrdersServiceGrpc.OrdersServiceImplBase.class, delegatesTo(
      new OrdersServiceGrpc.OrdersServiceImplBase() {
        @Override
        public void postOrder(PostOrderRequest request,
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

    verify(grpcService, times(2)).postOrder(eq(inArg), any());
  }

  @Test
  void postOrder_forbiddenInReadonly_Test() {
    var grpcService = mock(OrdersServiceGrpc.OrdersServiceImplBase.class);
    var readonlyService = mkClientBasedOnServer(
      grpcService,
      channel -> new OrdersService(
        OrdersStreamServiceGrpc.newStub(channel),
        OrdersServiceGrpc.newBlockingStub(channel),
        OrdersServiceGrpc.newStub(channel),
        true));

    assertThrows(
      ReadonlyModeViolationException.class,
      () -> readonlyService.postOrderSync(
        "", 0, Quotation.getDefaultInstance(), OrderDirection.ORDER_DIRECTION_UNSPECIFIED,
        "", OrderType.ORDER_TYPE_UNSPECIFIED, ""));
    futureThrown.expect(CompletionException.class);
    futureThrown.expectCause(IsInstanceOf.instanceOf(ReadonlyModeViolationException.class));
    readonlyService.postOrder(
      "", 0, Quotation.getDefaultInstance(), OrderDirection.ORDER_DIRECTION_UNSPECIFIED,
      "", OrderType.ORDER_TYPE_UNSPECIFIED, "");
  }

  @Test
  void getOrders_Test() {
    var accountId = "accountId";
    var expected = GetOrdersResponse.newBuilder()
      .addOrders(OrderState.newBuilder().setOrderId("orderId").build())
      .build();
    var grpcService = mock(OrdersServiceGrpc.OrdersServiceImplBase.class, delegatesTo(
      new OrdersServiceGrpc.OrdersServiceImplBase() {
        @Override
        public void getOrders(GetOrdersRequest request,
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
    verify(grpcService, times(2)).getOrders(eq(inArg), any());
  }

  @Test
  void cancelOrder_Test() {
    var accountId = "accountId";
    var orderId = "orderId";
    var expected = CancelOrderResponse.newBuilder()
      .setTime(Timestamp.newBuilder().setSeconds(1234567890).setNanos(0).build())
      .build();
    var grpcService = mock(OrdersServiceGrpc.OrdersServiceImplBase.class, delegatesTo(
      new OrdersServiceGrpc.OrdersServiceImplBase() {
        @Override
        public void cancelOrder(CancelOrderRequest request,
                                StreamObserver<CancelOrderResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var actualSync = service.cancelOrderSync(accountId, orderId);
    var actualAsync = service.cancelOrder(accountId, orderId).join();

    assertEquals(Helpers.timestampToInstant(expected.getTime()), actualSync);
    assertEquals(Helpers.timestampToInstant(expected.getTime()), actualAsync);

    var inArg = CancelOrderRequest.newBuilder()
      .setAccountId(accountId)
      .setOrderId(orderId)
      .build();
    verify(grpcService, times(2)).cancelOrder(eq(inArg), any());
  }

  @Test
  void cancelOrder_forbiddenInReadonly_Test() {
    var grpcService = mock(OrdersServiceGrpc.OrdersServiceImplBase.class);
    var readonlyService = mkClientBasedOnServer(
      grpcService,
      channel -> new OrdersService(
        OrdersStreamServiceGrpc.newStub(channel),
        OrdersServiceGrpc.newBlockingStub(channel),
        OrdersServiceGrpc.newStub(channel),
        true));

    assertThrows(
      ReadonlyModeViolationException.class,
      () -> readonlyService.cancelOrderSync("", ""));
    futureThrown.expect(CompletionException.class);
    futureThrown.expectCause(IsInstanceOf.instanceOf(ReadonlyModeViolationException.class));
    readonlyService.cancelOrder("", "");
  }

  @Test
  void getOrderState_Test() {
    var accountId = "accountId";
    var orderId = "orderId";
    var expected = OrderState.newBuilder()
      .setOrderId(orderId)
      .build();
    var grpcService = mock(OrdersServiceGrpc.OrdersServiceImplBase.class, delegatesTo(
      new OrdersServiceGrpc.OrdersServiceImplBase() {
        @Override
        public void getOrderState(GetOrderStateRequest request,
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
    verify(grpcService, times(2)).getOrderState(eq(inArg), any());
  }

}
