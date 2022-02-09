package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccruedInterest;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Dividend;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.MarketDataResponse;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.Order;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PositionsFutures;
import ru.tinkoff.piapi.contract.v1.PositionsSecurities;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.StopOrderDirection;
import ru.tinkoff.piapi.contract.v1.StopOrderType;
import ru.tinkoff.piapi.contract.v1.TradesStreamResponse;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

public class Example {
  static final Logger log = LoggerFactory.getLogger(Example.class);

  //Дешевая бумага, стоимостью 1-2 рубля за лот
  private static final String CHEAP_INSTRUMENT = "BBG00RPRPX12";

  public static void main(String[] args) {
    var token = args[0];
    var api = InvestApi.create(token);

    //unary
    instrumentsServiceExample(api);
    marketdataServiceExample(api);
    operationsServiceExample(api);
    ordersServiceExample(api);
    stopOrdersServiceExample(api);
    usersServiceExample(api);

    //streams
    marketdataStreamExample(api);
    ordersStreamExample(api);
  }

  private static void ordersStreamExample(InvestApi api) {
    Consumer<TradesStreamResponse> consumer = item -> {
      if (item.hasPing()) {
        log.info("пинг сообщение");
      } else if (item.hasOrderTrades()) {
        log.info("Новые данные по сделкам: {}", item);
      }
    };
    //блокирующий вызов
    api.getOrdersService().subscribeTradesStream(consumer);
  }

  private static List<String> randomFigi(InvestApi api, int count) {
    return api.getInstrumentsService().getTradableSharesSync()
      .stream()
      .filter(el -> Boolean.TRUE.equals(el.getApiTradeAvailableFlag()))
      .map(Share::getFigi)
      .limit(count)
      .collect(Collectors.toList());
  }

  private static void marketdataStreamExample(InvestApi api) {

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

    var randomFigi = randomFigi(api, 5);
    //блокирующие вызовы
    api.getMarketDataService().subscribeInfoStream(consumer, randomFigi);
    api.getMarketDataService().subscribeTradeStream(consumer, randomFigi);
    api.getMarketDataService().subscribeCandlesStream(consumer, randomFigi);
    api.getMarketDataService().subscribeOrderbookStream(consumer, randomFigi);
  }

  private static void usersServiceExample(InvestApi api) {
    //Получаем список аккаунтов и распечатываем их с указанием привилегий токена
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0);
    for (Account account : accounts) {
      log.info("account id: {}, access level: {}", account.getId(), account.getAccessLevel().name());
    }

    //Получаем и печатаем информацию о текущих лимитах пользователя
    var tariff = api.getUserService().getUserTariffSync();
    log.info("stream type: marketdata, stream limit: {}", tariff.getStreamLimitsList().get(0).getLimit());
    log.info("stream type: orders, stream limit: {}", tariff.getStreamLimitsList().get(1).getLimit());
    log.info("current unary limit per minute: {}", tariff.getUnaryLimitsList().get(0).getLimitPerMinute());

    //Получаем и печатаем информацию об обеспеченности портфеля
    var marginAttributes = api.getUserService().getMarginAttributesSync(mainAccount.getId());
    log.info("Ликвидная стоимость портфеля: {}", moneyValueToBigDecimal(marginAttributes.getLiquidPortfolio()));
    log.info("Начальная маржа — начальное обеспечение для совершения новой сделки: {}", moneyValueToBigDecimal(marginAttributes.getStartingMargin()));
    log.info("Минимальная маржа — это минимальное обеспечение для поддержания позиции, которую вы уже открыли: {}", moneyValueToBigDecimal(marginAttributes.getMinimalMargin()));
    log.info("Уровень достаточности средств. Соотношение стоимости ликвидного портфеля к начальной марже: {}", quotationToBigDecimal(marginAttributes.getFundsSufficiencyLevel()));
    log.info("Объем недостающих средств. Разница между стартовой маржой и ликвидной стоимости портфеля: {}", moneyValueToBigDecimal(marginAttributes.getAmountOfMissingFunds()));
  }


  private static void stopOrdersServiceExample(InvestApi api) {

    //Выставляем стоп-заявку
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();

    //Дешевая бумага, стоимостью 1-2 рубля за лот
    var figi = CHEAP_INSTRUMENT;
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

  private static void ordersServiceExample(InvestApi api) {
    //Выставляем заявку
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();

    //Дешевая бумага, стоимостью 1-2 рубля за лот
    var figi = CHEAP_INSTRUMENT;
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

  private static void operationsServiceExample(InvestApi api) {
    getOperationsExample(api);
    getPositionsExample(api);
    getPortfolioExample(api);
    getWithdrawLimitsExample(api);
  }

  private static void marketdataServiceExample(InvestApi api) {
    getCandlesExample(api);
    getOrderbookExample(api);
    getLastPricesExample(api);
    getTradingStatusExample(api);
  }


  private static void getWithdrawLimitsExample(InvestApi api) {
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();

    var withdrawLimits = api.getOperationsService().getWithdrawLimitsSync(mainAccount);
    var money = withdrawLimits.getMoneyList();
    var blocked = withdrawLimits.getBlockedList();
    var blockedGuarantee = withdrawLimits.getBlockedGuaranteeList();

    log.info("доступный для вывода остаток для счета {}", mainAccount);
    log.info("массив валютных позиций");
    for (MoneyValue moneyValue : money) {
      log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValueToBigDecimal(moneyValue));
    }

    log.info("массив заблокированных валютных позиций портфеля");
    for (MoneyValue moneyValue : blocked) {
      log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValueToBigDecimal(moneyValue));
    }

    log.info("заблокировано под гарантийное обеспечение фьючерсов");
    for (MoneyValue moneyValue : blockedGuarantee) {
      log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValueToBigDecimal(moneyValue));
    }
  }

  private static void getPortfolioExample(InvestApi api) {
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();

    //Получаем и печатаем портфолио
    var portfolio = api.getOperationsService().getPortfolioSync(mainAccount);
    var totalAmountBonds = portfolio.getTotalAmountBonds();
    log.info("общая стоимость облигаций в портфеле {}", moneyValueToBigDecimal(totalAmountBonds));

    var totalAmountEtf = portfolio.getTotalAmountEtf();
    log.info("общая стоимость фондов в портфеле {}", moneyValueToBigDecimal(totalAmountEtf));

    var totalAmountCurrencies = portfolio.getTotalAmountCurrencies();
    log.info("общая стоимость валют в портфеле {}", moneyValueToBigDecimal(totalAmountCurrencies));

    var totalAmountFutures = portfolio.getTotalAmountFutures();
    log.info("общая стоимость фьючерсов в портфеле {}", moneyValueToBigDecimal(totalAmountFutures));

    var totalAmountShares = portfolio.getTotalAmountShares();
    log.info("общая стоимость акций в портфеле {}", moneyValueToBigDecimal(totalAmountShares));

    log.info("текущая доходность портфеля {}", quotationToBigDecimal(portfolio.getExpectedYield()));

    var positions = portfolio.getPositionsList();
    log.info("в портфолио {} позиций", positions.size());
    for (int i = 0; i < Math.min(positions.size(), 5); i++) {
      var position = positions.get(i);
      var figi = position.getFigi();
      var quantity = position.getQuantity();
      var currentPrice = moneyValueToBigDecimal(position.getCurrentPrice());
      var expectedYield = quotationToBigDecimal(position.getExpectedYield());
      log.info("позиция с figi: {}, количество инструмента: {}, текущая цена инструмента: {}, текущая расчитанная доходность: {}", figi, quantity, currentPrice, expectedYield);
    }

  }

  private static void getPositionsExample(InvestApi api) {
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();
    //Получаем и печатаем список позиций
    var positions = api.getOperationsService().getPositionsSync(mainAccount);

    log.info("список валютных позиций портфеля");
    var moneyList = positions.getMoneyList();
    for (MoneyValue moneyValue : moneyList) {
      log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValueToBigDecimal(moneyValue));
    }

    log.info("список заблокированных валютных позиций портфеля");
    var blockedList = positions.getBlockedList();
    for (MoneyValue moneyValue : blockedList) {
      log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValueToBigDecimal(moneyValue));
    }

    log.info("список ценно-бумажных позиций портфеля");
    var securities = positions.getSecuritiesList();
    for (PositionsSecurities security : securities) {
      var figi = security.getFigi();
      var balance = security.getBalance();
      var blocked = security.getBlocked();
      log.info("figi: {}, текущий баланс: {}, заблокировано: {}", figi, balance, blocked);
    }

    log.info("список фьючерсов портфеля");
    var futuresList = positions.getFuturesList();
    for (PositionsFutures security : futuresList) {
      var figi = security.getFigi();
      var balance = security.getBalance();
      var blocked = security.getBlocked();
      log.info("figi: {}, текущий баланс: {}, заблокировано: {}", figi, balance, blocked);
    }
  }

  private static void getOperationsExample(InvestApi api) {
    var accounts = api.getUserService().getAccountsSync();
    var mainAccount = accounts.get(0).getId();

    //Получаем и печатаем список операций клиента
    var operations = api.getOperationsService().getOperationsSync(mainAccount, Instant.now().minus(30, ChronoUnit.DAYS), Instant.now(), OperationState.OPERATION_STATE_UNSPECIFIED, "");
    for (int i = 0; i < Math.min(operations.size(), 5); i++) {
      var operation = operations.get(i);
      var date = timestampToString(operation.getDate());
      var state = operation.getState().name();
      var id = operation.getId();
      var payment = moneyValueToBigDecimal(operation.getPayment());
      var figi = operation.getFigi();
      log.info("операция с id: {}, дата: {}, статус: {}, платеж: {}, figi: {}", id, date, state, payment, figi);
    }
  }

  private static void instrumentsServiceExample(InvestApi api) {
    //Получаем базовые списки инструментов и печатаем их
    var shares = api.getInstrumentsService().getTradableSharesSync();
    var etfs = api.getInstrumentsService().getTradableEtfsSync();
    var bonds = api.getInstrumentsService().getTradableBondsSync();
    var futures = api.getInstrumentsService().getTradableFuturesSync();
    var currencies = api.getInstrumentsService().getTradableCurrenciesSync();

    //Для 3 акций выводим список событий по выплате дивидендов
    for (int i = 0; i < Math.min(shares.size(), 3); i++) {
      var share = shares.get(i);
      var figi = share.getFigi();
      var dividends = api.getInstrumentsService().getDividendsSync(figi, Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS));
      for (Dividend dividend : dividends) {
        log.info("дивиденд для figi {}: {}", figi, dividend);
      }
    }

    //Для 3 облигаций выводим список НКД
    for (int i = 0; i < Math.min(bonds.size(), 3); i++) {
      var bond = bonds.get(i);
      var figi = bond.getFigi();
      var accruedInterests = api.getInstrumentsService().getAccruedInterestsSync(figi, Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS));
      for (AccruedInterest accruedInterest : accruedInterests) {
        log.info("НКД для figi {}: {}", figi, accruedInterest);
      }
    }

    //Для 3 фьючерсов выводим размер обеспечения
    for (int i = 0; i < Math.min(futures.size(), 3); i++) {
      var future = futures.get(i);
      var figi = future.getFigi();
      var futuresMargin = api.getInstrumentsService().getFuturesMarginSync(figi);
      log.info("гарантийное обеспечение при покупке для figi {}: {}", figi, moneyValueToBigDecimal(futuresMargin.getInitialMarginOnBuy()));
      log.info("гарантийное обеспечение при продаже для figi {}: {}", figi, moneyValueToBigDecimal(futuresMargin.getInitialMarginOnSell()));
      log.info("шаг цены figi для {}: {}", figi, quotationToBigDecimal(futuresMargin.getMinPriceIncrement()));
      log.info("стоимость шага цены для figi {}: {}", figi, quotationToBigDecimal(futuresMargin.getMinPriceIncrementAmount()));
    }

    //Получаем время работы биржи
    var tradingSchedules = api.getInstrumentsService().getTradingScheduleSync("spb", Instant.now(), Instant.now().plus(5, ChronoUnit.DAYS));
    for (TradingDay tradingDay : tradingSchedules.get().getDaysList()) {
      var date = timestampToString(tradingDay.getDate());
      var startDate = timestampToString(tradingDay.getStartTime());
      var endDate = timestampToString(tradingDay.getEndTime());
      if (tradingDay.getIsTradingDay()) {
        log.info("расписание торгов для площадки SPB. Дата: {},  открытие: {}, закрытие: {}", date, startDate, endDate);
      } else {
        log.info("расписание торгов для площадки SPB. Дата: {}. Выходной день", date);
      }
    }

    //Получаем инструмент по его figi
    var instrument = api.getInstrumentsService().getInstrumentByFigiSync("BBG000B9XRY4").get();
    log.info("инструмент figi: {}, лотность: {}, текущий режим торгов: {}, признак внебиржи: {}, признак доступности торгов через api : {}",
      instrument.getFigi(),
      instrument.getLot(),
      instrument.getTradingStatus().name(),
      instrument.getOtcFlag(),
      instrument.getApiTradeAvailableFlag());


    //Проверяем вывод ошибки в лог
    //Проверяем, что будет ошибка 50002. Об ошибках и причинах их возникновения - https://tinkoff.github.io/investAPI/errors/
    var bondFigi = "BBG00RPRPX12"; //инструмент с типом bond
    api.getInstrumentsService().getCurrencyByFigiSync(bondFigi);
  }

  private static void getTradingStatusExample(InvestApi api) {

    //Получаем и печатаем торговый статус инструмента
    var figi = randomFigi(api, 1).get(0);
    var tradingStatus = api.getMarketDataService().getTradingStatusSync(figi);
    log.info("торговый статус для инструмента {} - {}", figi, tradingStatus.getTradingStatus().name());
  }

  private static void getLastPricesExample(InvestApi api) {

    //Получаем и печатаем последнюю цену по инструменту
    var randomFigi = randomFigi(api, 5);
    var lastPrices = api.getMarketDataService().getLastPricesSync(randomFigi);
    for (LastPrice lastPrice : lastPrices) {
      var figi = lastPrice.getFigi();
      var price = quotationToBigDecimal(lastPrice.getPrice());
      var time = timestampToString(lastPrice.getTime());
      log.info("последняя цена по инструменту {}, цена: {}, время обновления цены: {}", figi, price, time);
    }

  }

  private static void getOrderbookExample(InvestApi api) {

    //Получаем и печатаем стакан для инструмента
    var figi = randomFigi(api, 1).get(0);
    var depth = 10;
    var orderBook = api.getMarketDataService().getOrderBookSync(figi, depth);
    var asks = orderBook.getAsksList();
    var bids = orderBook.getBidsList();
    var lastPrice = quotationToBigDecimal(orderBook.getLastPrice());
    var closePrice = quotationToBigDecimal(orderBook.getClosePrice());
    log.info("получен стакан по инструменту {}, глубина стакана: {}, количество предложений на покупку: {}, количество предложений на продажу: {}, цена последней сделки: {}, цена закрытия: {}",
      figi, depth, bids.size(), asks.size(), lastPrice, closePrice);

    log.info("предложения на покупку");
    for (Order bid : bids) {
      var price = quotationToBigDecimal(bid.getPrice());
      var quantity = bid.getQuantity();
      log.info("количество в лотах: {}, цена: {}", quantity, price);
    }

    log.info("предложения на продажу");
    for (Order ask : asks) {
      var price = quotationToBigDecimal(ask.getPrice());
      var quantity = ask.getQuantity();
      log.info("количество в лотах: {}, цена: {}", quantity, price);
    }
  }


  private static void getCandlesExample(InvestApi api) {

    //Получаем и печатаем список свечей для инструмента
    var figi = randomFigi(api, 1).get(0);
    var candles1min = api.getMarketDataService().getCandlesSync(figi, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), CandleInterval.CANDLE_INTERVAL_1_MIN);
    var candles5min = api.getMarketDataService().getCandlesSync(figi, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), CandleInterval.CANDLE_INTERVAL_5_MIN);
    var candles15min = api.getMarketDataService().getCandlesSync(figi, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), CandleInterval.CANDLE_INTERVAL_15_MIN);
    var candlesHour = api.getMarketDataService().getCandlesSync(figi, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), CandleInterval.CANDLE_INTERVAL_HOUR);
    var candlesDay = api.getMarketDataService().getCandlesSync(figi, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), CandleInterval.CANDLE_INTERVAL_DAY);

    log.info("получено {} 1-минутных свечей для инструмента с figi {}", candles1min.size(), figi);
    for (HistoricCandle candle : candles1min) {
      printCandle(candle);
    }

    log.info("получено {} 5-минутных свечей для инструмента с figi {}", candles5min.size(), figi);
    for (HistoricCandle candle : candles5min) {
      printCandle(candle);
    }

    log.info("получено {} 15-минутных свечей для инструмента с figi {}", candles15min.size(), figi);
    for (HistoricCandle candle : candles15min) {
      printCandle(candle);
    }

    log.info("получено {} 1-часовых свечей для инструмента с figi {}", candlesHour.size(), figi);
    for (HistoricCandle candle : candlesHour) {
      printCandle(candle);
    }

    log.info("получено {} 1-дневных свечей для инструмента с figi {}", candlesDay.size(), figi);
    for (HistoricCandle candle : candlesDay) {
      printCandle(candle);
    }
  }

  private static void printCandle(HistoricCandle candle) {
    var open = quotationToBigDecimal(candle.getOpen());
    var close = quotationToBigDecimal(candle.getClose());
    var high = quotationToBigDecimal(candle.getHigh());
    var low = quotationToBigDecimal(candle.getLow());
    var volume = candle.getVolume();
    var time = timestampToString(candle.getTime());
    log.info("цена открытия: {}, цена закрытия: {}, минимальная цена за 1 лот: {}, максимальная цена за 1 лот: {}, объем торгов в лотах: {}, время свечи: {}",
      open, close, low, high, volume, time);
  }
}
