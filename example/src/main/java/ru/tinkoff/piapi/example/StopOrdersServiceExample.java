package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.StopOrder;
import ru.tinkoff.piapi.contract.v1.StopOrderDirection;
import ru.tinkoff.piapi.contract.v1.StopOrderType;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

public class StopOrdersServiceExample {
  static final Logger log = LoggerFactory.getLogger(StopOrdersServiceExample.class);

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
    var stopPrice = Quotation.newBuilder().setUnits(lastPrice.getUnits() - minPriceIncrement.getUnits() * 100).setNano(lastPrice.getNano() - minPriceIncrement.getNano() * 100).build();
    var stopOrderId = api.getStopOrdersService().postStopOrderGoodTillDateSync(figi, 1, stopPrice, stopPrice, StopOrderDirection.STOP_ORDER_DIRECTION_BUY, mainAccount, StopOrderType.STOP_ORDER_TYPE_STOP_LOSS, Instant.now().plus(1, ChronoUnit.DAYS));
    log.info("выставлена стоп-заявка. id: {}", stopOrderId);

    //Получаем список стоп-заявок и смотрим, что наша заявка в ней есть
    var stopOrders = api.getStopOrdersService().getStopOrdersSync(mainAccount);
    stopOrders.stream().filter(el -> el.getStopOrderId().equals(stopOrderId)).findAny().orElseThrow();

    //Отменяем созданную стоп-заявку
    api.getStopOrdersService().cancelStopOrder(mainAccount, stopOrderId);
    log.info("стоп заявка с id {} отменена", stopOrderId);
  }

}
