package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.StopOrderDirection;
import ru.tinkoff.piapi.contract.v1.StopOrderType;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OrdersServiceExample {
  static final Logger log = LoggerFactory.getLogger(OrdersServiceExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);

    //Выставляем стоп-заявку
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();

    //Дешевая бумага, стоимостью 1-2 рубля за лот
    var figi = "BBG00RPRPX12";
    var lastPrice = api.getMarketDataService().getLastPricesSync(List.of(figi)).get(0).getPrice();
    var minPriceIncrement = api.getInstrumentsService().getInstrumentByFigiSync(figi).get().getMinPriceIncrement();
    var price = Quotation.newBuilder().setUnits(lastPrice.getUnits() - minPriceIncrement.getUnits() * 100).setNano(lastPrice.getNano() - minPriceIncrement.getNano() * 100).build();

    //Выставляем заявку на покупку по лимитной цене
    var orderId = api.getOrdersService().postOrderSync(figi, 1, price, OrderDirection.ORDER_DIRECTION_BUY, mainAccount, OrderType.ORDER_TYPE_LIMIT, UUID.randomUUID().toString()).getOrderId();

    //Получаем список активных заявок, проверяем наличие нашей заявки в списке
    var orders = api.getOrdersService().getOrdersSync(mainAccount);
    if (orders.stream().anyMatch(el -> orderId.equals(el.getOrderId()))) {
      log.info("заявка с id {} есть в списке активных заявок", orderId);
    }

    //Отменяем заявку
    api.getOrdersService().cancelOrder(mainAccount, orderId);
  }

}
