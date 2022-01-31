package ru.tinkoff.piapi.core;

import io.grpc.BindableService;
import io.grpc.Channel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;

import java.io.IOException;
import java.util.function.Function;

abstract class GrpcClientTester<T> {
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  abstract protected T createClient(Channel channel);

  final protected T mkClientBasedOnServer(BindableService grpcService, Function<Channel, T> customCreator) {
    var serverName = InProcessServerBuilder.generateName();
    try {
      grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
          .directExecutor()
          .addService(grpcService)
          .build()
          .start());
    } catch (IOException e) {
      System.err.println(e.getLocalizedMessage());
      System.exit(1);
    }

    var channel = grpcCleanup.register(
      InProcessChannelBuilder.forName(serverName)
        .directExecutor()
        .build());

    return customCreator.apply(channel);
  }

  final protected T mkClientBasedOnServer(BindableService grpcService) {
    var serverName = InProcessServerBuilder.generateName();
    try {
      grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
          .directExecutor()
          .addService(grpcService)
          .build()
          .start());
    } catch (IOException e) {
      System.err.println(e.getLocalizedMessage());
      System.exit(1);
    }

    var channel = grpcCleanup.register(
      InProcessChannelBuilder.forName(serverName)
        .directExecutor()
        .build());

    return createClient(channel);
  }
}
