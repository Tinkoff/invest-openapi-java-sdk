package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.function.Consumer;

public class MarketdataStreamExample {

  static final Logger log = LoggerFactory.getLogger(MarketdataServiceExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);

    infoStream(api);

  }

  private static void infoStream(InvestApi api) {
    var figi1 = "BBG000B9XRY4";
    var figi2 = "BBG004730N88";

    Consumer<MarketDataResponse> consumer = item -> {
      if (item.hasTradingStatus()) {
        log.info("Новые данные по статусам: {}", item);
      } else if (item.hasPing()) {
        log.info("пинг сообщение");
      } else if (item.hasCandle()) {
        log.info("Новые данные по свечам: {}", item);
      } else if (item.hasOrderbook()) {
        log.info("Новые данные по стакану: {}", item);
      } else if (item.hasTrade()) {
        log.info("Новые данные по сделкам: {}", item);
      }
    };
    api.getMarketDataService().subscribeInfoStream(List.of(figi1, figi2), consumer);
    api.getMarketDataService().subscribeTradeStream(List.of(figi1, figi2), consumer);
    api.getMarketDataService().subscribeCandlesStream(List.of(figi1, figi2), consumer);
    api.getMarketDataService().subscribeOrderbookStream(List.of(figi1, figi2), consumer);
  }
}
