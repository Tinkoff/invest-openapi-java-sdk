package ru.tinkoff.invest.openapi.example;

import ru.tinkoff.invest.openapi.automata.EntitiesAdaptor;
import ru.tinkoff.invest.openapi.automata.TradingAutomata;
import ru.tinkoff.invest.openapi.automata.TradingState;
import ru.tinkoff.invest.openapi.model.market.Instrument;
import ru.tinkoff.invest.openapi.model.portfolio.PortfolioCurrencies;
import ru.tinkoff.invest.openapi.model.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;
import ru.tinkoff.invest.openapi.okhttp.OkHttpSandboxOpenApi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.*;
import java.util.stream.Collectors;

public class App {
    private static String ssoToken;
    private static String ticker;
    private static TradingState.Candle.CandleInterval candleInterval;
    private static BigDecimal maxVolume;
    private static boolean useSandbox;

    public static void main(String[] args) {
        final Logger logger;

        try {
            logger = initLogger();
            extractParams(args, logger);
        } catch (IllegalArgumentException ex) {
            return;
        } catch (IOException ex) {
            System.err.println("При инициализации логгера произошла ошибка: " + ex.getLocalizedMessage());
            return;
        }

        try {
            logger.info("Создаём подключение... ");
            final var factory = new OkHttpOpenApiFactory(ssoToken, useSandbox, logger);
            final var automata = new TradingAutomata(factory, Executors.newScheduledThreadPool(10), logger);
            final var api = (OkHttpOpenApi) automata.api();

            if (useSandbox) {
                // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
                ((OkHttpSandboxOpenApi) api).performRegistration().join();
            }

            logger.info("Ищём по тикеру " + ticker + "... ");
            final var instrumentsList = api.searchMarketInstrumentsByTicker(ticker).join();

            final var instrumentOpt = instrumentsList.instruments.stream().findFirst();

            final Instrument instrument;
            if (instrumentOpt.isEmpty()) {
                logger.severe("Не нашлось инструмента с нужным тикером.");
                return;
            } else {
                instrument = instrumentOpt.get();
            }

            if (useSandbox) {
                initPrepareSandbox((OkHttpSandboxOpenApi) api, instrument, logger);
            }

            logger.info("Получаем валютные балансы... ");
            final var portfolioCurrencies = api.getPortfolioCurrencies().join();

            final var portfolioCurrencyOpt = portfolioCurrencies.currencies.stream()
                    .filter(pc -> pc.currency == instrument.currency)
                    .findFirst();

            final PortfolioCurrencies.PortfolioCurrency portfolioCurrency;
            if (portfolioCurrencyOpt.isEmpty()) {
                logger.severe("Не нашлось нужной валютной позиции.");
                return;
            } else {
                portfolioCurrency = portfolioCurrencyOpt.get();
                logger.info("Нужной валюты " + portfolioCurrency.currency + " на счету " + portfolioCurrency.balance.toPlainString());
            }

            final var currentOrders = api.getOrders().join();
            final var currentPositions = api.getPortfolio().join();

            final var strategy = new SimpleStopLossStrategy(
                    EntitiesAdaptor.convertApiEntityToTradingState(instrument),
                    currentOrders.stream().map(EntitiesAdaptor::convertApiEntityToTradingState).collect(Collectors.toList()),
                    portfolioCurrencies.currencies.stream()
                            .collect(Collectors.toMap(c -> EntitiesAdaptor.convertApiEntityToTradingState(c.currency), c -> new TradingState.Position(c.balance, c.blocked))),
                    currentPositions.positions.stream().collect(Collectors.toMap(p -> p.figi, p -> new TradingState.Position(p.balance, p.blocked))),
                    maxVolume,
                    5,
                    candleInterval,
                    BigDecimal.valueOf(0.1),
                    BigDecimal.valueOf(0.1),
                    BigDecimal.valueOf(0.3),
                    BigDecimal.valueOf(0.3),
                    BigDecimal.valueOf(0.2),
                    logger
            );

            initCleanupProcedure(automata, logger);

            logger.info("Запускаем робота... ");
            automata.addStrategy(strategy);
            automata.start();

            final var result = new CompletableFuture<Void>();
            result.join();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Что-то пошло не так.", ex);
        }
    }

    private static Logger initLogger() throws IOException {
        final var logManager = LogManager.getLogManager();
        final var classLoader = App.class.getClassLoader();

        try (InputStream input = classLoader.getResourceAsStream("logging.properties")) {

            if (input == null) {
                throw new FileNotFoundException();
            }

            Files.createDirectories(Paths.get("./logs"));
            logManager.readConfiguration(input);
        }

        return Logger.getLogger(App.class.getName());
    }

    private static void extractParams(final String[] args, final Logger logger) throws IllegalArgumentException {
        if (args.length == 0) {
            logger.severe("Не передан авторизационный токен!");
            throw new IllegalArgumentException();
        } else if (args.length == 1) {
            logger.severe("Не передан исследуемый тикер!");
            throw new IllegalArgumentException();
        } else if (args.length == 2) {
            logger.severe("Не передан разрешающий интервал свечей!");
            throw new IllegalArgumentException();
        } else if (args.length == 3) {
            logger.severe("Не передан допустимый объём используемых средств!");
            throw new IllegalArgumentException();
        } else if (args.length == 4) {
            logger.severe("Не передан признак использования песочницы!");
            throw new IllegalArgumentException();
        } else {
            ssoToken = args[0];
            ticker = args[1];
            switch (args[2]) {
                case "1min":
                    candleInterval = TradingState.Candle.CandleInterval.ONE_MIN;
                    break;
                case "2min":
                    candleInterval = TradingState.Candle.CandleInterval.TWO_MIN;
                    break;
                case "3min":
                    candleInterval = TradingState.Candle.CandleInterval.THREE_MIN;
                    break;
                case "5min":
                    candleInterval = TradingState.Candle.CandleInterval.FIVE_MIN;
                    break;
                case "10min":
                    candleInterval = TradingState.Candle.CandleInterval.TEN_MIN;
                    break;
                default:
                    logger.severe("Не распознан разрешающий интервал!");
                    throw new IllegalArgumentException();
            }
            maxVolume = new BigDecimal(args[3]);
            useSandbox = Boolean.parseBoolean(args[4]);
        }
    }

    private static void initPrepareSandbox(final OkHttpSandboxOpenApi context,
                                           final Instrument instrument,
                                           final Logger logger) {
        logger.info("Очищаем всё позиции... ");
        context.clearAll().join();

        logger.info("Ставим на баланс немного " + instrument.currency + "... ");
        final CurrencyBalance cb = new CurrencyBalance(instrument.currency, maxVolume);
        context.setCurrencyBalance(cb).join();
    }

    private static void initCleanupProcedure(final TradingAutomata automata, final Logger logger) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Закрываем соединение... ");
                automata.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Что-то произошло при закрытии соединения!", e);
            }
        }));
    }
}
