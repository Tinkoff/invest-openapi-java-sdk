package ru.tinkoff.invest.openapi.example;

import ru.tinkoff.invest.openapi.automata.EntitiesAdaptor;
import ru.tinkoff.invest.openapi.automata.TradingAutomata;
import ru.tinkoff.invest.openapi.automata.TradingState;
import ru.tinkoff.invest.openapi.model.Currency;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.*;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) {
        final Logger logger;
        try {
            logger = initLogger();
        } catch (IOException ex) {
            System.err.println("При инициализации логгера произошла ошибка: " + ex.getLocalizedMessage());
            return;
        }

        final TradingParameters parameters;
        try {
            parameters = extractParams(args);
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Не удалось извлечь торговые параметры.", ex);
            return;
        }

        try {
            logger.info("Создаём подключение... ");
            final var factory = new OkHttpOpenApiFactory(parameters.ssoToken, parameters.sandboxMode, logger);
            final var automata = new TradingAutomata(factory, Executors.newScheduledThreadPool(10), logger);
            final var api = (OkHttpOpenApi) automata.api();

            if (parameters.sandboxMode) {
                // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
                ((OkHttpSandboxOpenApi) api).performRegistration().join();
            }

            final Map<Currency, BigDecimal> currencyVolumes = new HashMap<>();
            for (int i = 0; i < parameters.tickers.length; i++) {
                final var ticker = parameters.tickers[i];
                final var candleInterval = parameters.candleIntervals[i];
                final var maxVolume = parameters.maxVolumes[i];

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

                currencyVolumes.put(
                        instrument.currency,
                        currencyVolumes.getOrDefault(instrument.currency, BigDecimal.ZERO)
                                .add(maxVolume)
                );

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

                automata.addStrategy(strategy);
            }

            if (parameters.sandboxMode) {
                initPrepareSandbox((OkHttpSandboxOpenApi) api, currencyVolumes, logger);
            }

            initCleanupProcedure(automata, logger);

            logger.info("Запускаем робота... ");
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

    private static TradingParameters extractParams(final String[] args) throws IllegalArgumentException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Не передан авторизационный токен!");
        } else if (args.length == 1) {
            throw new IllegalArgumentException("Не передан исследуемый тикер!");
        } else if (args.length == 2) {
            throw new IllegalArgumentException("Не передан разрешающий интервал свечей!");
        } else if (args.length == 3) {
            throw new IllegalArgumentException("Не передан допустимый объём используемых средств!");
        } else if (args.length == 4) {
            throw new IllegalArgumentException("Не передан признак использования песочницы!");
        } else {
            return TradingParameters.fromProgramArgs(args[0], args[1], args[2], args[3], args[4]);
        }
    }

    private static void initPrepareSandbox(final OkHttpSandboxOpenApi context,
                                           final Map<Currency, BigDecimal> currencyVolumes,
                                           final Logger logger) {
        currencyVolumes.forEach((key, value) -> {
            logger.info("Ставим на баланс немного " + key + "... ");
            final var cb = new CurrencyBalance(key, value);
            context.setCurrencyBalance(cb).join();
        });
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
