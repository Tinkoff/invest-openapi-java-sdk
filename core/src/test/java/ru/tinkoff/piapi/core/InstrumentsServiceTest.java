package ru.tinkoff.piapi.core;

import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import ru.tinkoff.piapi.contract.v1.*;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class InstrumentsServiceTest extends GrpcClientTester<InstrumentsService> {

  @Rule
  public ExpectedException futureThrown = ExpectedException.none();

  @Override
  protected InstrumentsService createClient(Channel channel) {
    return new InstrumentsService(
      InstrumentsServiceGrpc.newBlockingStub(channel),
      InstrumentsServiceGrpc.newStub(channel));
  }

  @Nested
  class GetTradingSchedulesTest {

    @Test
    void getAllSchedules_Test() {
      var expected = TradingSchedulesResponse.newBuilder()
        .addExchanges(TradingSchedule.newBuilder().setExchange("MOEX").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void tradingSchedules(TradingSchedulesRequest request,
                                       StreamObserver<TradingSchedulesResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = TradingSchedulesRequest.newBuilder()
        .setFrom(Timestamp.newBuilder().setSeconds(1234567890).build())
        .setTo(Timestamp.newBuilder().setSeconds(1234567890).setNanos(111222333).build())
        .build();
      var actualSync = service.getTradingSchedulesSync(
        Helpers.timestampToInstant(inArg.getFrom()),
        Helpers.timestampToInstant(inArg.getTo()));
      var actualAsync = service.getTradingSchedules(
        Helpers.timestampToInstant(inArg.getFrom()),
        Helpers.timestampToInstant(inArg.getTo())).join();

      assertIterableEquals(expected.getExchangesList(), actualSync);
      assertIterableEquals(expected.getExchangesList(), actualAsync);

      verify(grpcService, times(2)).tradingSchedules(eq(inArg), any());
    }

    @Test
    void getAllSchedules_shouldThrowIfToIsNotAfterFrom_Test() {
      var expected = TradingSchedulesResponse.newBuilder()
        .addExchanges(TradingSchedule.newBuilder().setExchange("MOEX").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void tradingSchedules(TradingSchedulesRequest request,
                                       StreamObserver<TradingSchedulesResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var now = Instant.now();
      var nowMinusSecond = now.minusSeconds(1);
      assertThrows(IllegalArgumentException.class, () -> service.getTradingSchedulesSync(now, nowMinusSecond));
      futureThrown.expect(CompletionException.class);
      futureThrown.expectCause(IsInstanceOf.instanceOf(IllegalArgumentException.class));
      service.getTradingSchedules(now, nowMinusSecond);

      verify(grpcService, never()).tradingSchedules(any(), any());
    }

    @Test
    void getOneSchedule_Test() {
      var exchange = "MOEX";
      var expected = TradingSchedulesResponse.newBuilder()
        .addExchanges(TradingSchedule.newBuilder().setExchange(exchange).build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void tradingSchedules(TradingSchedulesRequest request,
                                       StreamObserver<TradingSchedulesResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = TradingSchedulesRequest.newBuilder()
        .setExchange(exchange)
        .setFrom(Timestamp.newBuilder().setSeconds(1234567890).build())
        .setTo(Timestamp.newBuilder().setSeconds(1234567890).setNanos(111222333).build())
        .build();
      var actualSync = service.getTradingScheduleSync(
        exchange,
        Helpers.timestampToInstant(inArg.getFrom()),
        Helpers.timestampToInstant(inArg.getTo()));
      var actualAsync = service.getTradingSchedule(
        exchange,
        Helpers.timestampToInstant(inArg.getFrom()),
        Helpers.timestampToInstant(inArg.getTo())).join();

      assertEquals(Optional.of(expected.getExchangesList().get(0)), actualSync);
      assertEquals(Optional.of(expected.getExchangesList().get(0)), actualAsync);

      verify(grpcService, times(2)).tradingSchedules(eq(inArg), any());
    }

    @Test
    void getAllSchedule_shouldThrowIfToIsNotAfterFrom_Test() {
      var exchange = "MOEX";
      var expected = TradingSchedulesResponse.newBuilder()
        .addExchanges(TradingSchedule.newBuilder().setExchange(exchange).build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void tradingSchedules(TradingSchedulesRequest request,
                                       StreamObserver<TradingSchedulesResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var now = Instant.now();
      var nowMinusSecond = now.minusSeconds(1);
      assertThrows(IllegalArgumentException.class, () -> service.getTradingScheduleSync(exchange, now, nowMinusSecond));
      futureThrown.expect(CompletionException.class);
      futureThrown.expectCause(IsInstanceOf.instanceOf(IllegalArgumentException.class));
      service.getTradingSchedule(exchange, now, nowMinusSecond);

      verify(grpcService, never()).tradingSchedules(any(), any());
    }

    @Test
    void getOneSchedule_shouldReturnNoneInCaseOfNotFoundStatus_Test() {
      var exchange = "MOEX";
      var expected = Optional.<TradingSchedule>empty();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void tradingSchedules(TradingSchedulesRequest request,
                                       StreamObserver<TradingSchedulesResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = TradingSchedulesRequest.newBuilder()
        .setExchange(exchange)
        .setFrom(Timestamp.newBuilder().setSeconds(1234567890).build())
        .setTo(Timestamp.newBuilder().setSeconds(1234567890).setNanos(111222333).build())
        .build();
      var actualSync = service.getTradingScheduleSync(
        exchange,
        Helpers.timestampToInstant(inArg.getFrom()),
        Helpers.timestampToInstant(inArg.getTo()));
      var actualAsync = service.getTradingSchedule(
        exchange,
        Helpers.timestampToInstant(inArg.getFrom()),
        Helpers.timestampToInstant(inArg.getTo())).join();

      assertEquals(expected, actualSync);
      assertEquals(expected, actualAsync);

      verify(grpcService, times(2)).tradingSchedules(eq(inArg), any());
    }

  }

  @Nested
  class GetBondsTest {

    @Test
    void getOneByTicker_Test() {
      var expected = BondResponse.newBuilder()
        .setInstrument(Bond.newBuilder().setTicker("TCS").setClassCode("moex").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void bondBy(InstrumentRequest request,
                             StreamObserver<BondResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
        .setId("TCS")
        .setClassCode("moex")
        .build();
      var actualSync = service.getBondByTickerSync(inArg.getId(), inArg.getClassCode());
      var actualAsync = service.getBondByTicker(inArg.getId(), inArg.getClassCode()).join();

      assertEquals(Optional.of(expected.getInstrument()), actualSync);
      assertEquals(Optional.of(expected.getInstrument()), actualAsync);

      verify(grpcService, times(2)).bondBy(eq(inArg), any());
    }

    @Test
    void getOneByTicker_shouldReturnEmptyInCaseOfNotFound_Test() {
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void bondBy(InstrumentRequest request,
                             StreamObserver<BondResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
        .setId("TCS")
        .setClassCode("moex")
        .build();
      var actualSync = service.getBondByTickerSync(inArg.getId(), inArg.getClassCode());
      var actualAsync = service.getBondByTicker(inArg.getId(), inArg.getClassCode()).join();

      assertEquals(Optional.<Bond>empty(), actualSync);
      assertEquals(Optional.<Bond>empty(), actualAsync);

      verify(grpcService, times(2)).bondBy(eq(inArg), any());
    }

    @Test
    void getOneByFigi_Test() {
      var expected = BondResponse.newBuilder()
        .setInstrument(Bond.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void bondBy(InstrumentRequest request,
                             StreamObserver<BondResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
        .setId("figi")
        .build();
      var actualSync = service.getBondByFigiSync(inArg.getId());
      var actualAsync = service.getBondByFigi(inArg.getId()).join();

      assertEquals(Optional.of(expected.getInstrument()), actualSync);
      assertEquals(Optional.of(expected.getInstrument()), actualAsync);

      verify(grpcService, times(2)).bondBy(eq(inArg), any());
    }

    @Test
    void getOneByFigi_shouldReturnEmptyInCaseOfNotFound_Test() {
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void bondBy(InstrumentRequest request,
                             StreamObserver<BondResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
        .setId("figi")
        .build();
      var actualSync = service.getBondByFigiSync(inArg.getId());
      var actualAsync = service.getBondByFigi(inArg.getId()).join();

      assertEquals(Optional.<Bond>empty(), actualSync);
      assertEquals(Optional.<Bond>empty(), actualAsync);

      verify(grpcService, times(2)).bondBy(eq(inArg), any());
    }

    @Test
    void getTradable_Test() {
      var expected = BondsResponse.newBuilder()
        .addInstruments(Bond.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void bonds(InstrumentsRequest request,
                            StreamObserver<BondsResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentsRequest.newBuilder()
        .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_BASE)
        .build();
      var actualSync = service.getTradableBondsSync();
      var actualAsync = service.getTradableBonds().join();

      assertIterableEquals(expected.getInstrumentsList(), actualSync);
      assertIterableEquals(expected.getInstrumentsList(), actualAsync);

      verify(grpcService, times(2)).bonds(eq(inArg), any());
    }

    @Test
    void getAll_Test() {
      var expected = BondsResponse.newBuilder()
        .addInstruments(Bond.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void bonds(InstrumentsRequest request,
                            StreamObserver<BondsResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentsRequest.newBuilder()
        .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_ALL)
        .build();
      var actualSync = service.getAllBondsSync();
      var actualAsync = service.getAllBonds().join();

      assertIterableEquals(expected.getInstrumentsList(), actualSync);
      assertIterableEquals(expected.getInstrumentsList(), actualAsync);

      verify(grpcService, times(2)).bonds(eq(inArg), any());
    }

  }

  @Nested
  class GetCurrenciesTest {

    @Test
    void getOneByTicker_Test() {
      var expected = CurrencyResponse.newBuilder()
        .setInstrument(Currency.newBuilder().setTicker("TCS").setClassCode("moex").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void currencyBy(InstrumentRequest request,
                                 StreamObserver<CurrencyResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
        .setId("TCS")
        .setClassCode("moex")
        .build();
      var actualSync = service.getCurrencyByTickerSync(inArg.getId(), inArg.getClassCode());
      var actualAsync = service.getCurrencyByTicker(inArg.getId(), inArg.getClassCode()).join();

      assertEquals(Optional.of(expected.getInstrument()), actualSync);
      assertEquals(Optional.of(expected.getInstrument()), actualAsync);

      verify(grpcService, times(2)).currencyBy(eq(inArg), any());
    }

    @Test
    void getOneByTicker_shouldReturnEmptyInCaseOfNotFound_Test() {
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void currencyBy(InstrumentRequest request,
                                 StreamObserver<CurrencyResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
        .setId("TCS")
        .setClassCode("moex")
        .build();
      var actualSync = service.getCurrencyByTickerSync(inArg.getId(), inArg.getClassCode());
      var actualAsync = service.getCurrencyByTicker(inArg.getId(), inArg.getClassCode()).join();

      assertEquals(Optional.<Currency>empty(), actualSync);
      assertEquals(Optional.<Currency>empty(), actualAsync);

      verify(grpcService, times(2)).currencyBy(eq(inArg), any());
    }

    @Test
    void getOneByFigi_Test() {
      var expected = CurrencyResponse.newBuilder()
        .setInstrument(Currency.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void currencyBy(InstrumentRequest request,
                                 StreamObserver<CurrencyResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
        .setId("figi")
        .build();
      var actualSync = service.getCurrencyByFigiSync(inArg.getId());
      var actualAsync = service.getCurrencyByFigi(inArg.getId()).join();

      assertEquals(Optional.of(expected.getInstrument()), actualSync);
      assertEquals(Optional.of(expected.getInstrument()), actualAsync);

      verify(grpcService, times(2)).currencyBy(eq(inArg), any());
    }

    @Test
    void getOneByFigi_shouldReturnEmptyInCaseOfNotFound_Test() {
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void currencyBy(InstrumentRequest request,
                                 StreamObserver<CurrencyResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
        .setId("figi")
        .build();
      var actualSync = service.getCurrencyByFigiSync(inArg.getId());
      var actualAsync = service.getCurrencyByFigi(inArg.getId()).join();

      assertEquals(Optional.<Currency>empty(), actualSync);
      assertEquals(Optional.<Currency>empty(), actualAsync);

      verify(grpcService, times(2)).currencyBy(eq(inArg), any());
    }

    @Test
    void getTradable_Test() {
      var expected = CurrenciesResponse.newBuilder()
        .addInstruments(Currency.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void currencies(InstrumentsRequest request,
                                 StreamObserver<CurrenciesResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentsRequest.newBuilder()
        .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_BASE)
        .build();
      var actualSync = service.getTradableCurrenciesSync();
      var actualAsync = service.getTradableCurrencies().join();

      assertIterableEquals(expected.getInstrumentsList(), actualSync);
      assertIterableEquals(expected.getInstrumentsList(), actualAsync);

      verify(grpcService, times(2)).currencies(eq(inArg), any());
    }

    @Test
    void getAll_Test() {
      var expected = CurrenciesResponse.newBuilder()
        .addInstruments(Currency.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void currencies(InstrumentsRequest request,
                                 StreamObserver<CurrenciesResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentsRequest.newBuilder()
        .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_ALL)
        .build();
      var actualSync = service.getAllCurrenciesSync();
      var actualAsync = service.getAllCurrencies().join();

      assertIterableEquals(expected.getInstrumentsList(), actualSync);
      assertIterableEquals(expected.getInstrumentsList(), actualAsync);

      verify(grpcService, times(2)).currencies(eq(inArg), any());
    }

  }

  @Nested
  class GetEtfsTest {

    @Test
    void getOneByTicker_Test() {
      var expected = EtfResponse.newBuilder()
        .setInstrument(Etf.newBuilder().setTicker("TCS").setClassCode("moex").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void etfBy(InstrumentRequest request,
                            StreamObserver<EtfResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
        .setId("TCS")
        .setClassCode("moex")
        .build();
      var actualSync = service.getEtfByTickerSync(inArg.getId(), inArg.getClassCode());
      var actualAsync = service.getEtfByTicker(inArg.getId(), inArg.getClassCode()).join();

      assertEquals(Optional.of(expected.getInstrument()), actualSync);
      assertEquals(Optional.of(expected.getInstrument()), actualAsync);

      verify(grpcService, times(2)).etfBy(eq(inArg), any());
    }

    @Test
    void getOneByTicker_shouldReturnEmptyInCaseOfNotFound_Test() {
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void etfBy(InstrumentRequest request,
                            StreamObserver<EtfResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
        .setId("TCS")
        .setClassCode("moex")
        .build();
      var actualSync = service.getEtfByTickerSync(inArg.getId(), inArg.getClassCode());
      var actualAsync = service.getEtfByTicker(inArg.getId(), inArg.getClassCode()).join();

      assertEquals(Optional.<Etf>empty(), actualSync);
      assertEquals(Optional.<Etf>empty(), actualAsync);

      verify(grpcService, times(2)).etfBy(eq(inArg), any());
    }

    @Test
    void getOneByFigi_Test() {
      var expected = EtfResponse.newBuilder()
        .setInstrument(Etf.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void etfBy(InstrumentRequest request,
                            StreamObserver<EtfResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
        .setId("figi")
        .build();
      var actualSync = service.getEtfByFigiSync(inArg.getId());
      var actualAsync = service.getEtfByFigi(inArg.getId()).join();

      assertEquals(Optional.of(expected.getInstrument()), actualSync);
      assertEquals(Optional.of(expected.getInstrument()), actualAsync);

      verify(grpcService, times(2)).etfBy(eq(inArg), any());
    }

    @Test
    void getOneByFigi_shouldReturnEmptyInCaseOfNotFound_Test() {
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void etfBy(InstrumentRequest request,
                            StreamObserver<EtfResponse> responseObserver) {
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND));
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentRequest.newBuilder()
        .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
        .setId("figi")
        .build();
      var actualSync = service.getEtfByFigiSync(inArg.getId());
      var actualAsync = service.getEtfByFigi(inArg.getId()).join();

      assertEquals(Optional.<Etf>empty(), actualSync);
      assertEquals(Optional.<Etf>empty(), actualAsync);

      verify(grpcService, times(2)).etfBy(eq(inArg), any());
    }

    @Test
    void getTradable_Test() {
      var expected = EtfsResponse.newBuilder()
        .addInstruments(Etf.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void etfs(InstrumentsRequest request,
                           StreamObserver<EtfsResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentsRequest.newBuilder()
        .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_BASE)
        .build();
      var actualSync = service.getTradableEtfsSync();
      var actualAsync = service.getTradableEtfs().join();

      assertIterableEquals(expected.getInstrumentsList(), actualSync);
      assertIterableEquals(expected.getInstrumentsList(), actualAsync);

      verify(grpcService, times(2)).etfs(eq(inArg), any());
    }

    @Test
    void getAll_Test() {
      var expected = EtfsResponse.newBuilder()
        .addInstruments(Etf.newBuilder().setFigi("figi").build())
        .build();
      var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
        new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
          @Override
          public void etfs(InstrumentsRequest request,
                           StreamObserver<EtfsResponse> responseObserver) {
            responseObserver.onNext(expected);
            responseObserver.onCompleted();
          }
        }));
      var service = mkClientBasedOnServer(grpcService);

      var inArg = InstrumentsRequest.newBuilder()
        .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_ALL)
        .build();
      var actualSync = service.getAllEtfsSync();
      var actualAsync = service.getAllEtfs().join();

      assertIterableEquals(expected.getInstrumentsList(), actualSync);
      assertIterableEquals(expected.getInstrumentsList(), actualAsync);

      verify(grpcService, times(2)).etfs(eq(inArg), any());
    }

  }

  @Test
  void getFutureByTicker_Test() {
    var expected = FutureResponse.newBuilder()
      .setInstrument(Future.newBuilder().setTicker("TCS").setClassCode("moex").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void futureBy(InstrumentRequest request,
                             StreamObserver<FutureResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentRequest.newBuilder()
      .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
      .setId("TCS")
      .setClassCode("moex")
      .build();
    var actualSync = service.getFutureByTickerSync(inArg.getId(), inArg.getClassCode());
    var actualAsync = service.getFutureByTicker(inArg.getId(), inArg.getClassCode()).join();

    assertEquals(Optional.of(expected.getInstrument()), actualSync);
    assertEquals(Optional.of(expected.getInstrument()), actualAsync);

    verify(grpcService, times(2)).futureBy(eq(inArg), any());
  }

  @Test
  void getFutureByFigi_Test() {
    var expected = FutureResponse.newBuilder()
      .setInstrument(Future.newBuilder().setFigi("figi").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void futureBy(InstrumentRequest request,
                             StreamObserver<FutureResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentRequest.newBuilder()
      .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
      .setId("figi")
      .build();
    var actualSync = service.getFutureByFigiSync(inArg.getId());
    var actualAsync = service.getFutureByFigi(inArg.getId()).join();

    assertEquals(Optional.of(expected.getInstrument()), actualSync);
    assertEquals(Optional.of(expected.getInstrument()), actualAsync);

    verify(grpcService, times(2)).futureBy(eq(inArg), any());
  }

  @Test
  void getFutures_Test() {
    var expected = FuturesResponse.newBuilder()
      .addInstruments(Future.newBuilder().setFigi("figi").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void futures(InstrumentsRequest request,
                            StreamObserver<FuturesResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentsRequest.newBuilder()
      .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_ALL)
      .build();
    var actualSync = service.getFuturesSync(inArg.getInstrumentStatus());
    var actualAsync = service.getFutures(inArg.getInstrumentStatus()).join();

    assertIterableEquals(expected.getInstrumentsList(), actualSync);
    assertIterableEquals(expected.getInstrumentsList(), actualAsync);

    verify(grpcService, times(2)).futures(eq(inArg), any());
  }

  @Test
  void getShareByTicker_Test() {
    var expected = ShareResponse.newBuilder()
      .setInstrument(Share.newBuilder().setTicker("TCS").setClassCode("moex").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void shareBy(InstrumentRequest request,
                            StreamObserver<ShareResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentRequest.newBuilder()
      .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
      .setId("TCS")
      .setClassCode("moex")
      .build();
    var actualSync = service.getShareByTickerSync(inArg.getId(), inArg.getClassCode());
    var actualAsync = service.getShareByTicker(inArg.getId(), inArg.getClassCode()).join();

    assertEquals(Optional.of(expected.getInstrument()), actualSync);
    assertEquals(Optional.of(expected.getInstrument()), actualAsync);

    verify(grpcService, times(2)).shareBy(eq(inArg), any());
  }

  @Test
  void getShareByFigi_Test() {
    var expected = ShareResponse.newBuilder()
      .setInstrument(Share.newBuilder().setFigi("figi").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void shareBy(InstrumentRequest request,
                            StreamObserver<ShareResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentRequest.newBuilder()
      .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
      .setId("figi")
      .build();
    var actualSync = service.getShareByFigiSync(inArg.getId());
    var actualAsync = service.getShareByFigi(inArg.getId()).join();

    assertEquals(Optional.of(expected.getInstrument()), actualSync);
    assertEquals(Optional.of(expected.getInstrument()), actualAsync);

    verify(grpcService, times(2)).shareBy(eq(inArg), any());
  }

  @Test
  void getShares_Test() {
    var expected = SharesResponse.newBuilder()
      .addInstruments(Share.newBuilder().setFigi("figi").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void shares(InstrumentsRequest request,
                           StreamObserver<SharesResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentsRequest.newBuilder()
      .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_ALL)
      .build();
    var actualSync = service.getSharesSync(inArg.getInstrumentStatus());
    var actualAsync = service.getShares(inArg.getInstrumentStatus()).join();

    assertIterableEquals(expected.getInstrumentsList(), actualSync);
    assertIterableEquals(expected.getInstrumentsList(), actualAsync);

    verify(grpcService, times(2)).shares(eq(inArg), any());
  }

  @Test
  void getAccruedInterests_Test() {
    var expected = GetAccruedInterestsResponse.newBuilder()
      .addAccruedInterests(AccruedInterest.newBuilder().setValuePercent(3.14f).build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void getAccruedInterests(GetAccruedInterestsRequest request,
                                        StreamObserver<GetAccruedInterestsResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetAccruedInterestsRequest.newBuilder()
      .setFigi("figi")
      .setFrom(Timestamp.newBuilder().setSeconds(1234567890).build())
      .setTo(Timestamp.newBuilder().setSeconds(1234567890).setNanos(111222333).build())
      .build();
    var actualSync = service.getAccruedInterestsSync(
      inArg.getFigi(),
      Helpers.timestampToInstant(inArg.getFrom()),
      Helpers.timestampToInstant(inArg.getTo()));
    var actualAsync = service.getAccruedInterests(
      inArg.getFigi(),
      Helpers.timestampToInstant(inArg.getFrom()),
      Helpers.timestampToInstant(inArg.getTo())).join();

    assertIterableEquals(expected.getAccruedInterestsList(), actualSync);
    assertIterableEquals(expected.getAccruedInterestsList(), actualAsync);

    verify(grpcService, times(2)).getAccruedInterests(eq(inArg), any());
  }

  @Test
  void getFuturesMargin_Test() {
    var expected = GetFuturesMarginResponse.newBuilder()
      .setInitialMarginOnBuy(MoneyValue.newBuilder().setCurrency("USD").setUnits(1).build())
      .setInitialMarginOnSell(MoneyValue.newBuilder().setCurrency("USD").setUnits(2).build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void getFuturesMargin(GetFuturesMarginRequest request,
                                     StreamObserver<GetFuturesMarginResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetFuturesMarginRequest.newBuilder()
      .setFigi("figi")
      .build();
    var actualSync = service.getFuturesMarginSync(inArg.getFigi());
    var actualAsync = service.getFuturesMargin(inArg.getFigi()).join();

    assertEquals(expected, actualSync);
    assertEquals(expected, actualAsync);

    verify(grpcService, times(2)).getFuturesMargin(eq(inArg), any());
  }

  @Test
  void getInstrumentByTicker_Test() {
    var expected = InstrumentResponse.newBuilder()
      .setInstrument(Instrument.newBuilder().setTicker("TCS").setClassCode("moex").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void getInstrumentBy(InstrumentRequest request,
                                    StreamObserver<InstrumentResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentRequest.newBuilder()
      .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_TICKER)
      .setId("TCS")
      .setClassCode("moex")
      .build();
    var actualSync = service.getInstrumentByTickerSync(inArg.getId(), inArg.getClassCode());
    var actualAsync = service.getInstrumentByTicker(inArg.getId(), inArg.getClassCode()).join();

    assertEquals(Optional.of(expected.getInstrument()), actualSync);
    assertEquals(Optional.of(expected.getInstrument()), actualAsync);

    verify(grpcService, times(2)).getInstrumentBy(eq(inArg), any());
  }

  @Test
  void getInstrumentByFigi_Test() {
    var expected = InstrumentResponse.newBuilder()
      .setInstrument(Instrument.newBuilder().setFigi("figi").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void getInstrumentBy(InstrumentRequest request,
                                    StreamObserver<InstrumentResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = InstrumentRequest.newBuilder()
      .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_FIGI)
      .setId("figi")
      .build();
    var actualSync = service.getInstrumentByFigiSync(inArg.getId());
    var actualAsync = service.getInstrumentByFigi(inArg.getId()).join();

    assertEquals(Optional.of(expected.getInstrument()), actualSync);
    assertEquals(Optional.of(expected.getInstrument()), actualAsync);

    verify(grpcService, times(2)).getInstrumentBy(eq(inArg), any());
  }

  @Test
  void getDividends_Test() {
    var expected = GetDividendsResponse.newBuilder()
      .addDividends(Dividend.newBuilder().setDividendType("Regular Cash").build())
      .build();
    var grpcService = mock(InstrumentsServiceGrpc.InstrumentsServiceImplBase.class, delegatesTo(
      new InstrumentsServiceGrpc.InstrumentsServiceImplBase() {
        @Override
        public void getDividends(GetDividendsRequest request,
                                 StreamObserver<GetDividendsResponse> responseObserver) {
          responseObserver.onNext(expected);
          responseObserver.onCompleted();
        }
      }));
    var service = mkClientBasedOnServer(grpcService);

    var inArg = GetDividendsRequest.newBuilder()
      .setFigi("figi")
      .setFrom(Timestamp.newBuilder().setSeconds(1234567890).build())
      .setTo(Timestamp.newBuilder().setSeconds(1234567890).setNanos(111222333).build())
      .build();
    var actualSync = service.getDividendsSync(
      inArg.getFigi(),
      Helpers.timestampToInstant(inArg.getFrom()),
      Helpers.timestampToInstant(inArg.getTo()));
    var actualAsync = service.getDividends(
      inArg.getFigi(),
      Helpers.timestampToInstant(inArg.getFrom()),
      Helpers.timestampToInstant(inArg.getTo())).join();

    assertIterableEquals(expected.getDividendsList(), actualSync);
    assertIterableEquals(expected.getDividendsList(), actualAsync);

    verify(grpcService, times(2)).getDividends(eq(inArg), any());
  }

}
