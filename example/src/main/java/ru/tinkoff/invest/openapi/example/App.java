package ru.tinkoff.invest.openapi.example;

import ru.tinkoff.invest.openapi.SimpleStopLossStrategy;
import ru.tinkoff.invest.openapi.StrategyExecutor;
import ru.tinkoff.invest.openapi.data.CandleInterval;
import ru.tinkoff.invest.openapi.data.Instrument;
import ru.tinkoff.invest.openapi.wrapper.Connection;
import ru.tinkoff.invest.openapi.wrapper.Context;
import ru.tinkoff.invest.openapi.wrapper.SandboxContext;
import ru.tinkoff.invest.openapi.wrapper.impl.ConnectionFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.logging.*;

public class App {
    private static String ssoToken;
    private static String ticker;
    private static CandleInterval candleInterval;
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
            final Connection connection;
            final Context context;
            if (useSandbox) {
                logger.fine("Создаём подключение в режиме \"песочницы\"... ");
                connection = ConnectionFactory.connectSandbox(ssoToken, logger).join();
                context = connection.context();
            } else {
                logger.fine("Создаём подключение в биржевом режиме... ");
                connection = ConnectionFactory.connect(ssoToken, logger).join();
                context = connection.context();
            }

            logger.fine("Ищём по тикеру " + ticker + "... ");
            final var instrumentsList = context.searchMarketInstrumentsByTicker(ticker).join();

            final var instrumentOpt = instrumentsList.getInstruments()
                    .stream()
                    .findFirst();

            final Instrument instrument;
            if (instrumentOpt.isEmpty()) {
                logger.severe("Не нашлось инструмента с нужным тикером.");
                return;
            } else {
                instrument = instrumentOpt.get();
            }

            if (useSandbox) {
                initPrepareSandbox((SandboxContext)context, instrument, logger);
            }

            logger.fine("Получаем активные заявки... ");
            final var orders = context.getOrders().join();

            logger.fine("Получаем валютные балансы... ");
            final var portfolioCurrencies = context.getPortfolioCurrencies().join();

            final var portfolioCurrencyOpt = portfolioCurrencies.getCurrencies().stream()
                    .filter(pc -> pc.getCurrency() == instrument.getCurrency())
                    .findFirst();

            if (portfolioCurrencyOpt.isEmpty()) {
                logger.severe("Не нашлось нужной валютной позиции.");
                return;
            }

            logger.fine("Запускаем робота... ");
            final CompletableFuture<Void> result = new CompletableFuture<>();
            final var strategy = new SimpleStopLossStrategy(
                    portfolioCurrencyOpt.get(),
                    orders,
                    instrument,
                    maxVolume,
                    5,
                    candleInterval,
                    BigDecimal.valueOf(0.1),
                    BigDecimal.valueOf(0.1),
                    BigDecimal.valueOf(0.5),
                    BigDecimal.valueOf(0.5),
                    logger
            );
            final var strategyExecutor = new StrategyExecutor(context, strategy, logger);
            strategyExecutor.run();
            result.join();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Что-то пошло не так.", ex);
        }
    }

    private static Logger initLogger() throws IOException {
        LogManager logManager = LogManager.getLogManager();
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
                    candleInterval = CandleInterval.ONE_MIN;
                    break;
                case "2min":
                    candleInterval = CandleInterval.TWO_MIN;
                    break;
                case "3min":
                    candleInterval = CandleInterval.THREE_MIN;
                    break;
                case "5min":
                    candleInterval = CandleInterval.FIVE_MIN;
                    break;
                case "10min":
                    candleInterval = CandleInterval.TEN_MIN;
                    break;
                default:
                    logger.severe("Не распознан разрешающий интервал!");
                    throw new IllegalArgumentException();
            }
            maxVolume = new BigDecimal(args[3]);
            useSandbox = Boolean.parseBoolean(args[4]);
        }
    }

    private static void initPrepareSandbox(final SandboxContext context,
                                           final Instrument instrument,
                                           final Logger logger) {
        logger.fine("Очищаем всё позиции... ");
        context.clearAll().join();

        logger.fine("Ставим на баланс немного " + instrument.getCurrency() + "... ");
        context.setCurrencyBalance(instrument.getCurrency(), maxVolume).join();
    }
}
