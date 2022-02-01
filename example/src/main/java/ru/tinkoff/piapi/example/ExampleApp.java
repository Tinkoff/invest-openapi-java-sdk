package ru.tinkoff.piapi.example;

import io.smallrye.mutiny.Multi;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.InvestApi;

public class ExampleApp {
  static final Logger logger = LoggerFactory.getLogger(ExampleApp.class);

  public static void main(String[] args) {
    var token = args[0];
    var sandbox = InvestApi.createSandbox(InvestApi.defaultChannel(token));

    // Пример синхронного (блокирующего) вызова для получения списка валют.
    var currencies = sandbox.getInstrumentsService()
      .getTradableCurrenciesSync();

    // Пример ассинхронного вызова для получения списка счетов в "песочнице".
    sandbox.getSandboxService().getAccounts()
      .thenAccept(accounts -> {
        for (var account : accounts) {
          logger.info("Счёт {}", account);
        }
      });

    // Пример stream-вызова со статусом инструментов.
    // На вход подаётся поток запросов.
    var request = FlowAdapters.toFlowPublisher(
      Multi.createFrom().<MarketDataRequest>emitter(emitter -> {
        emitter.emit(mkInstrumentInfoRequest(currencies.get(0).getFigi()));
        emitter.emit(mkInstrumentInfoRequest(currencies.get(1).getFigi()));
        // emitter.complete(); <-- Если останавить поток запросов, то остановиться и поток ответов.
      }));
    // На выходе поток ответов.
    Multi.createFrom()
      .safePublisher(
        FlowAdapters.toPublisher(
          sandbox.getMarketDataService().marketDataStream(request)))
      .subscribe()
      .asIterable()
      .forEach(item -> logger.info("Новые данные: {}", item));
  }

  static MarketDataRequest mkInstrumentInfoRequest(String figi) {
    return MarketDataRequest.newBuilder()
      .setSubscribeInfoRequest(
        SubscribeInfoRequest.newBuilder()
          .setSubscriptionAction(
            SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE)
          .addInstruments(
            InfoInstrument.newBuilder().setFigi(figi).build())
          .build())
      .build();
  }
}
