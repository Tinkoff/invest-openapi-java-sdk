package ru.tinkoff.piapi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InvestApiTest {

  // TODO Как проверить, что в настройках по умолчанию задан TARGET из конфига?
//  @Test
//  void defaultChannelUsesTargetFromConfig() {
//
//  }

  // TODO Как проверить, что в настройках по умолчанию задан CONNECTION_TIMEOUT из конфига?
//  @Test
//  void defaultChannelUsesConnectionTimeoutFromConfig() {
//
//  }

  // TODO Как проверить, что в настройках по умолчанию задан REQUEST_TIMEOUT из конфига?
//  @Test
//  void defaultChannelUsesRequestTimeoutFromConfig() {
//
//  }

  @Test
  void creationAlwaysUsesPassedChannel() {
    var channel = InvestApi.defaultChannel("token");

    var api = InvestApi.create(channel);
    assertSame(channel, api.getChannel(), "Simple creation doesn't use passed Channel.");

    var readonlyApi = InvestApi.createReadonly(channel);
    assertSame(channel, readonlyApi.getChannel(), "Readonly creation doesn't use passed Channel.");

    var sandboxApi = InvestApi.createSandbox(channel);
    assertSame(channel, sandboxApi.getChannel(), "Sandbox creation doesn't use passed Channel.");
  }

  @Test
  void simpleCreationProducesNotReadonlyNorSandbox() {
    var channel = InvestApi.defaultChannel("token");

    var api = InvestApi.create(channel);
    assertFalse(api.isReadonlyMode(), "Simple creation produces readonly mode.");
    assertFalse(api.isSandboxMode(), "Simple creation produces sandbox mode.");
  }

  @Test
  void readonlyCreationProducesReadonlyOnly() {
    var channel = InvestApi.defaultChannel("token");

    var readonlyApi = InvestApi.createReadonly(channel);
    assertTrue(readonlyApi.isReadonlyMode(), "Readonly creation doesn't produce readonly mode.");
    assertFalse(readonlyApi.isSandboxMode(), "Readonly creation produces sandbox mode.");
  }

  @Test
  void sandboxCreationProducesSandboxOnly() {
    var channel = InvestApi.defaultChannel("token");

    var sandboxApi = InvestApi.createSandbox(channel);
    assertFalse(sandboxApi.isReadonlyMode(), "Sandbox creation produces readonly mode.");
    assertTrue(sandboxApi.isSandboxMode(), "Sandbox creation doesn't produce sandbox mode.");
  }

  @Test
  void ordersServiceIsDisallowedInSandboxMode() {
    var channel = InvestApi.defaultChannel("token");

    var sandboxApi = InvestApi.createSandbox(channel);
    assertThrows(IllegalStateException.class, sandboxApi::getOrdersService);
  }

  @Test
  void stopOrdersServiceIsDisallowedInSandboxMode() {
    var channel = InvestApi.defaultChannel("token");

    var sandboxApi = InvestApi.createSandbox(channel);
    assertThrows(IllegalStateException.class, sandboxApi::getStopOrdersService);
  }

  @Test
  void operationsServiceIsDisallowedInSandboxMode() {
    var channel = InvestApi.defaultChannel("token");

    var sandboxApi = InvestApi.createSandbox(channel);
    assertThrows(IllegalStateException.class, sandboxApi::getOperationsService);
  }

  @Test
  void userServiceIsDisallowedInSandboxMode() {
    var channel = InvestApi.defaultChannel("token");

    var sandboxApi = InvestApi.createSandbox(channel);
    assertThrows(IllegalStateException.class, sandboxApi::getUserService);
  }

  @Test
  void sandboxServiceIsAllowedInSandboxModeOnly() {
    var channel = InvestApi.defaultChannel("token");

    var api = InvestApi.create(channel);
    assertThrows(IllegalStateException.class, api::getSandboxService);
    var readonlyApi = InvestApi.createReadonly(channel);
    assertThrows(IllegalStateException.class, readonlyApi::getSandboxService);
  }

  @Test
  void instrumentsServiceIsAlwaysAllowed() {
    var channel = InvestApi.defaultChannel("token");

    var api = InvestApi.create(channel);
    assertDoesNotThrow(api::getInstrumentsService);
    var readonlyApi = InvestApi.createReadonly(channel);
    assertDoesNotThrow(readonlyApi::getInstrumentsService);
    var sandboxApi = InvestApi.createReadonly(channel);
    assertDoesNotThrow(sandboxApi::getInstrumentsService);
  }

  @Test
  void marketDataServiceIsAlwaysAllowed() {
    var channel = InvestApi.defaultChannel("token");

    var api = InvestApi.create(channel);
    assertDoesNotThrow(api::getMarketDataService);
    var readonlyApi = InvestApi.createReadonly(channel);
    assertDoesNotThrow(readonlyApi::getMarketDataService);
    var sandboxApi = InvestApi.createReadonly(channel);
    assertDoesNotThrow(sandboxApi::getMarketDataService);
  }
}
