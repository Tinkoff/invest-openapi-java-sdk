package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.function.Consumer;

public class OrdersStreamExample {
  static final Logger log = LoggerFactory.getLogger(MarketdataServiceExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);

    tradesStream(api);

  }

  private static void tradesStream(InvestApi api) {

    Consumer<TradesStreamResponse> consumer = item -> {
      if (item.hasPing()) {
        log.info("пинг сообщение");
      } else if (item.hasOrderTrades()) {
        log.info("Новые данные по сделкам: {}", item);
      }
    };
    api.getOrdersService().subscribeTradesStream(consumer);
  }

}
