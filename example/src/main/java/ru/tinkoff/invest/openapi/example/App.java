package ru.tinkoff.invest.openapi.example;

import io.reactivex.rxjava3.core.Flowable;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.SandboxRegisterRequest;
import ru.tinkoff.invest.openapi.model.streaming.StreamingRequest;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.logging.LogManager;

public class App {

    static org.slf4j.Logger logger;

    public static void main(String[] args) {
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
            logger.error("Не удалось извлечь торговые параметры.", ex);
            return;
        }

        try (final OpenApi api = new OkHttpOpenApi(parameters.ssoToken, parameters.sandboxMode)) {
            logger.info("Создаём подключение... ");
            if (api.isSandboxMode()) {
                // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
                api.getSandboxContext().performRegistration(new SandboxRegisterRequest()).join();
            }

            final var stopNotifier = new CompletableFuture<Void>();
            final var rxStreaming = Flowable.fromPublisher(api.getStreamingContext());
            final var rxSubscription = rxStreaming
                    .doOnError(stopNotifier::completeExceptionally)
                    .doOnComplete(() -> stopNotifier.complete(null))
                    .forEach(event -> logger.info("Пришло новое событие из Streaming API\n" + event));

            final var currentOrders = api.getOrdersContext().getOrders(null).join();
            logger.info("Количество текущих заявок: " + currentOrders.size());
            final var currentPositions = api.getPortfolioContext().getPortfolio(null).join();
            logger.info("Количество текущих позиций: " + currentPositions.getPositions().size());

            for (int i = 0; i < parameters.tickers.length; i++) {
                final var ticker = parameters.tickers[i];
                final var candleInterval = parameters.candleIntervals[i];

                logger.info("Ищём по тикеру " + ticker + "... ");
                final var instrumentsList = api.getMarketContext().searchMarketInstrumentsByTicker(ticker).join();

                final var instrumentOpt = instrumentsList.getInstruments().stream().findFirst();

                final MarketInstrument instrument;
                if (instrumentOpt.isEmpty()) {
                    logger.error("Не нашлось инструмента с нужным тикером.");
                    return;
                } else {
                    instrument = instrumentOpt.get();
                }

                logger.info("Получаем валютные балансы... ");
                final var portfolioCurrencies = api.getPortfolioContext().getPortfolioCurrencies(null).join();

                final var portfolioCurrencyOpt = portfolioCurrencies.getCurrencies().stream()
                        .filter(pc -> pc.getCurrency() == instrument.getCurrency())
                        .findFirst();

                final CurrencyPosition portfolioCurrency;
                if (portfolioCurrencyOpt.isEmpty()) {
                    logger.error("Не нашлось нужной валютной позиции.");
                    return;
                } else {
                    portfolioCurrency = portfolioCurrencyOpt.get();
                    logger.info("Нужной валюты " + portfolioCurrency.getCurrency() + " на счету " + portfolioCurrency.getBalance().toPlainString());
                }

                api.getStreamingContext().sendRequest(StreamingRequest.subscribeCandle(instrument.getFigi(), candleInterval));
            }

            stopNotifier.join();
        } catch (final Exception ex) {
            logger.error("Что-то пошло не так.", ex);
        }
    }

    private static org.slf4j.Logger initLogger() throws IOException {
        final var logManager = LogManager.getLogManager();
        final var classLoader = App.class.getClassLoader();

        try (final InputStream input = classLoader.getResourceAsStream("logging.properties")) {

            if (input == null) {
                throw new FileNotFoundException();
            }

            Files.createDirectories(Paths.get("./logs"));
            logManager.readConfiguration(input);
        }

        return org.slf4j.LoggerFactory.getLogger(App.class);
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

}
