package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.AccruedInterest;
import ru.tinkoff.piapi.contract.v1.Dividend;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

public class InstrumentsExample {
  static final Logger log = LoggerFactory.getLogger(InstrumentsExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);


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
}
