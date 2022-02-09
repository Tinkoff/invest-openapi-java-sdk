package ru.tinkoff.piapi.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.core.InvestApi;

import static ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

public class UsersServiceExample {
  static final Logger log = LoggerFactory.getLogger(UsersServiceExample.class);

  public static void main(String[] args) {
    var token = "t.my_token";
    var api = InvestApi.create(token);

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

}
