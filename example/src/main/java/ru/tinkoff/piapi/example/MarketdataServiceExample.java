package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.GetOrderBookResponse;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusResponse;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Order;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.utils.DateUtils;
import ru.tinkoff.piapi.core.utils.MapperUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.*;

public class MarketdataServiceExample {
  static final Logger log = LoggerFactory.getLogger(MarketdataServiceExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);

    getCandlesExample(api);
    getOrderbookExample(api);
    getLastPricesExample(api);
    getTradingStatusExample(api);

  }

  private static void getTradingStatusExample(InvestApi api) {

    //Получаем и печатаем торговый статус инструмента
    var figi = "BBG000B9XRY4";
    var tradingStatus = api.getMarketDataService().getTradingStatusSync(figi);
    log.info("торговый статус для инструмента {} - {}", figi, tradingStatus.getTradingStatus().name());
  }

  private static void getLastPricesExample(InvestApi api) {

    //Получаем и печатаем последнюю цену по инструменту
    var figi1 = "BBG000B9XRY4";
    var figi2 = "BBG004730N88";
    var lastPrices = api.getMarketDataService().getLastPricesSync(List.of(figi1, figi2));
    for (LastPrice lastPrice : lastPrices) {
      var figi = lastPrice.getFigi();
      var price = quotationToBigDecimal(lastPrice.getPrice());
      var time = timestampToString(lastPrice.getTime());
      log.info("последняя цена по инструменту {}, цена: {}, время обновления цены: {}", figi, price, time);
    }

  }

  private static void getOrderbookExample(InvestApi api) {

    //Получаем и печатаем стакан для инструмента
    var figi = "BBG000B9XRY4";
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
    var figi = "BBG000B9XRY4";
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
