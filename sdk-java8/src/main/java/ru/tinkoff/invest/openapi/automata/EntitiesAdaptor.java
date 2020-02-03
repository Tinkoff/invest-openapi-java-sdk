package ru.tinkoff.invest.openapi.automata;

import ru.tinkoff.invest.openapi.model.Currency;
import ru.tinkoff.invest.openapi.model.MoneyAmount;
import ru.tinkoff.invest.openapi.model.market.CandleInterval;
import ru.tinkoff.invest.openapi.model.orders.Operation;
import ru.tinkoff.invest.openapi.model.orders.Order;
import ru.tinkoff.invest.openapi.model.orders.Status;

public class EntitiesAdaptor {

    private EntitiesAdaptor() {
    }

    public static TradingState.Candle.CandleInterval convertApiEntityToTradingState(CandleInterval apiCandleInterval) {
        switch (apiCandleInterval) {
            case ONE_MIN:
                return TradingState.Candle.CandleInterval.ONE_MIN;
            case TWO_MIN:
                return TradingState.Candle.CandleInterval.TWO_MIN;
            case THREE_MIN:
                return TradingState.Candle.CandleInterval.THREE_MIN;
            case FIVE_MIN:
                return TradingState.Candle.CandleInterval.FIVE_MIN;
            case TEN_MIN:
                return TradingState.Candle.CandleInterval.TEN_MIN;
            case QUARTER_HOUR:
                return TradingState.Candle.CandleInterval.QUARTER_HOUR;
            case HALF_HOUR:
                return TradingState.Candle.CandleInterval.HALF_HOUR;
            case HOUR:
                return TradingState.Candle.CandleInterval.HOUR;
            case DAY:
                return TradingState.Candle.CandleInterval.DAY;
            case WEEK:
                return TradingState.Candle.CandleInterval.WEEK;
            case MONTH:
                return TradingState.Candle.CandleInterval.MONTH;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static TradingState.Order.Status convertApiEntityToTradingState(Status apiStatus) {
        switch (apiStatus) {
            case New:
                return TradingState.Order.Status.New;
            case PartiallyFill:
                return TradingState.Order.Status.PartiallyFill;
            case Fill:
                return TradingState.Order.Status.Fill;
            case Cancelled:
                return TradingState.Order.Status.Cancelled;
            case Replaced:
                return TradingState.Order.Status.Replaced;
            case PendingCancel:
                return TradingState.Order.Status.PendingCancel;
            case Rejected:
                return TradingState.Order.Status.Rejected;
            case PendingReplace:
                return TradingState.Order.Status.PendingReplace;
            case PendingNew:
                return TradingState.Order.Status.PendingNew;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static TradingState.Order.Type convertApiEntityToTradingState(Operation apiOperation) {
        switch (apiOperation) {
            case Buy:
                return TradingState.Order.Type.Buy;
            case Sell:
                return TradingState.Order.Type.Sell;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static TradingState.MoneyAmount convertApiEntityToTradingState(MoneyAmount apiMoney) {
        return new TradingState.MoneyAmount(convertApiEntityToTradingState(apiMoney.currency), apiMoney.value);
    }

    public static TradingState.Currency convertApiEntityToTradingState(Currency apiCurrency) {
        switch (apiCurrency) {
            case RUB:
                return TradingState.Currency.RUB;
            case USD:
                return TradingState.Currency.USD;
            case EUR:
                return TradingState.Currency.EUR;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static CandleInterval convertTradingStateToApiEntity(TradingState.Candle.CandleInterval domainCandleInterval) {
        switch (domainCandleInterval) {
            case ONE_MIN:
                return CandleInterval.ONE_MIN;
            case TWO_MIN:
                return CandleInterval.TWO_MIN;
            case THREE_MIN:
                return CandleInterval.THREE_MIN;
            case FIVE_MIN:
                return CandleInterval.FIVE_MIN;
            case TEN_MIN:
                return CandleInterval.TEN_MIN;
            case QUARTER_HOUR:
                return CandleInterval.QUARTER_HOUR;
            case HALF_HOUR:
                return CandleInterval.HALF_HOUR;
            case HOUR:
                return CandleInterval.HOUR;
            case DAY:
                return CandleInterval.DAY;
            case WEEK:
                return CandleInterval.WEEK;
            case MONTH:
                return CandleInterval.MONTH;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Operation convertTradingStateToApiEntity(TradingState.Order.Type domainOperationType) {
        switch (domainOperationType) {
            case Buy:
                return Operation.Buy;
            case Sell:
                return Operation.Sell;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Instrument convertApiEntityToTradingState(ru.tinkoff.invest.openapi.model.market.Instrument apiInstrument) {
        return new Instrument(
                apiInstrument.figi,
                apiInstrument.minPriceIncrement,
                apiInstrument.lot,
                convertApiEntityToTradingState(apiInstrument.currency)
        );
    }

    public static TradingState.Order convertApiEntityToTradingState(Order apiOrder) {
        return new TradingState.Order(
                apiOrder.id,
                convertApiEntityToTradingState(apiOrder.operation),
                convertApiEntityToTradingState(apiOrder.status),
                apiOrder.requestedLots,
                apiOrder.executedLots,
                apiOrder.price,
                apiOrder.figi
        );
    }

}
