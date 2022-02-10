package ru.tinkoff.piapi.core;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.BackPressureStrategy;
import org.reactivestreams.FlowAdapters;
import ru.tinkoff.piapi.contract.v1.CandleInstrument;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.GetCandlesRequest;
import ru.tinkoff.piapi.contract.v1.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.GetLastPricesRequest;
import ru.tinkoff.piapi.contract.v1.GetLastPricesResponse;
import ru.tinkoff.piapi.contract.v1.GetOrderBookRequest;
import ru.tinkoff.piapi.contract.v1.GetOrderBookResponse;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusRequest;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusResponse;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.InfoInstrument;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.MarketDataRequest;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc;
import ru.tinkoff.piapi.contract.v1.MarketDataStreamServiceGrpc;
import ru.tinkoff.piapi.contract.v1.OrderBookInstrument;
import ru.tinkoff.piapi.contract.v1.SubscribeCandlesRequest;
import ru.tinkoff.piapi.contract.v1.SubscribeInfoRequest;
import ru.tinkoff.piapi.contract.v1.SubscribeOrderBookRequest;
import ru.tinkoff.piapi.contract.v1.SubscribeTradesRequest;
import ru.tinkoff.piapi.contract.v1.SubscriptionAction;
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval;
import ru.tinkoff.piapi.contract.v1.TradeInstrument;
import ru.tinkoff.piapi.core.utils.DateUtils;
import ru.tinkoff.piapi.core.utils.Helpers;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.function.Consumer;

public class MarketDataService {
  private final MarketDataStreamServiceGrpc.MarketDataStreamServiceStub marketDataStreamStub;
  private final MarketDataServiceGrpc.MarketDataServiceBlockingStub marketDataBlockingStub;
  private final MarketDataServiceGrpc.MarketDataServiceStub marketDataStub;

  MarketDataService(
    @Nonnull MarketDataStreamServiceGrpc.MarketDataStreamServiceStub marketDataStreamStub,
    @Nonnull MarketDataServiceGrpc.MarketDataServiceBlockingStub marketDataBlockingStub,
    @Nonnull MarketDataServiceGrpc.MarketDataServiceStub marketDataStub) {
    this.marketDataStreamStub = marketDataStreamStub;
    this.marketDataBlockingStub = marketDataBlockingStub;
    this.marketDataStub = marketDataStub;
  }

  @Nonnull
  public Publisher<MarketDataResponse> marketDataStream(
    @Nonnull Publisher<MarketDataRequest> requestsPublisher) {
    var mutinyPublisher = Multi.createFrom().<MarketDataResponse>emitter(
      emitter -> {
        var requestsSubscriber = marketDataStreamStub.marketDataStream(
          Helpers.wrapEmitterWithStreamObserver(emitter));

        Multi.createFrom()
          .publisher(FlowAdapters.toPublisher(requestsPublisher))
          .subscribe()
          .with(
            requestsSubscriber::onNext,
            requestsSubscriber::onError,
            requestsSubscriber::onCompleted);
      },
      BackPressureStrategy.BUFFER);

    return FlowAdapters.toFlowPublisher(mutinyPublisher);
  }

  @Nonnull
  public List<HistoricCandle> getCandlesSync(
    @Nonnull String figi,
    @Nonnull Instant from,
    @Nonnull Instant to,
    @Nonnull CandleInterval interval) {
    return marketDataBlockingStub.getCandles(
        GetCandlesRequest.newBuilder()
          .setFigi(figi)
          .setFrom(DateUtils.instantToTimestamp(from))
          .setTo(DateUtils.instantToTimestamp(to))
          .setInterval(interval)
          .build())
      .getCandlesList();
  }

  @Nonnull
  public List<LastPrice> getLastPricesSync(@Nonnull Iterable<String> figies) {
    return marketDataBlockingStub.getLastPrices(
        GetLastPricesRequest.newBuilder()
          .addAllFigi(figies)
          .build())
      .getLastPricesList();
  }

  @Nonnull
  public GetOrderBookResponse getOrderBookSync(@Nonnull String figi, int depth) {
    return marketDataBlockingStub.getOrderBook(
      GetOrderBookRequest.newBuilder()
        .setFigi(figi)
        .setDepth(depth)
        .build());
  }

  @Nonnull
  public GetTradingStatusResponse getTradingStatusSync(@Nonnull String figi) {
    return marketDataBlockingStub.getTradingStatus(
      GetTradingStatusRequest.newBuilder()
        .setFigi(figi)
        .build());
  }

  @Nonnull
  public CompletableFuture<List<HistoricCandle>> getCandles(
    @Nonnull String figi,
    @Nonnull Instant from,
    @Nonnull Instant to,
    @Nonnull CandleInterval interval) {
    return Helpers.<GetCandlesResponse>wrapWithFuture(
        observer -> marketDataStub.getCandles(
          GetCandlesRequest.newBuilder()
            .setFigi(figi)
            .setFrom(DateUtils.instantToTimestamp(from))
            .setTo(DateUtils.instantToTimestamp(to))
            .setInterval(interval)
            .build(),
          observer))
      .thenApply(GetCandlesResponse::getCandlesList);
  }

  @Nonnull
  public CompletableFuture<List<LastPrice>> getLastPrices(@Nonnull Iterable<String> figies) {
    return Helpers.<GetLastPricesResponse>wrapWithFuture(
        observer -> marketDataStub.getLastPrices(
          GetLastPricesRequest.newBuilder()
            .addAllFigi(figies)
            .build(),
          observer))
      .thenApply(GetLastPricesResponse::getLastPricesList);
  }

  @Nonnull
  public CompletableFuture<GetOrderBookResponse> getOrderBook(@Nonnull String figi, int depth) {
    return Helpers.wrapWithFuture(
      observer -> marketDataStub.getOrderBook(
        GetOrderBookRequest.newBuilder()
          .setFigi(figi)
          .setDepth(depth)
          .build(),
        observer));
  }

  @Nonnull
  public CompletableFuture<GetTradingStatusResponse> getTradingStatus(@Nonnull String figi) {
    return Helpers.wrapWithFuture(
      observer -> marketDataStub.getTradingStatus(
        GetTradingStatusRequest.newBuilder()
          .setFigi(figi)
          .build(),
        observer));
  }
}
