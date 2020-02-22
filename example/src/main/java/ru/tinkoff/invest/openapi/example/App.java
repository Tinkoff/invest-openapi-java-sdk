package ru.tinkoff.invest.openapi.example;

import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;
import ru.tinkoff.invest.openapi.models.streaming.StreamingRequest;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;
import ru.tinkoff.invest.openapi.okhttp.OkHttpSandboxOpenApi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.logging.*;

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
            final var api = factory.createOpenApiClient(
                    se -> logger.info("Из Streaming API пришло событие"),
                    ex -> logger.severe("Что-то произошло со Streaming API")
            );

            if (parameters.sandboxMode) {
                // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
                ((OkHttpSandboxOpenApi) api).sandboxContext.performRegistration(null).join();
            }

            final var currentOrders = api.ordersContext.getOrders(null).join();
            logger.info("Количество текущих заявок: " + currentOrders.size());
            final var currentPositions = api.portfolioContext.getPortfolio(null).join();
            logger.info("Количество текущих позиций: " + currentPositions.positions.size());

            for (int i = 0; i < parameters.tickers.length; i++) {
                final var ticker = parameters.tickers[i];
                final var candleInterval = parameters.candleIntervals[i];

                logger.info("Ищём по тикеру " + ticker + "... ");
                final var instrumentsList = api.marketContext.searchMarketInstrumentsByTicker(ticker).join();

                final var instrumentOpt = instrumentsList.instruments.stream().findFirst();

                final Instrument instrument;
                if (instrumentOpt.isEmpty()) {
                    logger.severe("Не нашлось инструмента с нужным тикером.");
                    return;
                } else {
                    instrument = instrumentOpt.get();
                }

                logger.info("Получаем валютные балансы... ");
                final var portfolioCurrencies = api.portfolioContext.getPortfolioCurrencies(null).join();

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

                api.streamingContext.sendRequest(StreamingRequest.subscribeCandle(instrument.figi, candleInterval));
            }

            initCleanupProcedure(api.streamingContext, logger);

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
            throw new IllegalArgumentException("Не передан признак использования песочницы!");
        } else {
            return TradingParameters.fromProgramArgs(args[0], args[1], args[2], args[3]);
        }
    }

    private static void initCleanupProcedure(final StreamingContext sc, final Logger logger) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Закрываем соединение... ");
                sc.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Что-то произошло при закрытии соединения!", e);
            }
        }));
    }

}
