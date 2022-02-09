package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.PortfolioResponse;
import ru.tinkoff.piapi.contract.v1.PositionsFutures;
import ru.tinkoff.piapi.contract.v1.PositionsSecurities;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

public class OperationsServiceExample {
  static final Logger log = LoggerFactory.getLogger(OperationsServiceExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);

    getOperationsExample(api);
    getPositionsExample(api);
    getPortfolioExample(api);
    getWithdrawLimitsExample(api);


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
      log.info("операция с id: {}, дата: {}, статус: {}, платеж: {}, figi: {}", id, date, state,payment, figi);
    }
  }

}
