package nl.hayovanloon.grpc.simpleserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;


public class IntegerArithmeticClient implements AutoCloseable {

  private final ManagedChannel channel;
  private final IntegerArithmeticGrpc.IntegerArithmeticBlockingStub
      blockingStub;

  IntegerArithmeticClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = IntegerArithmeticGrpc.newBlockingStub(channel);
  }

  public static IntegerArithmeticClient of(String host, int port) {
    final ManagedChannel managedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build();
    return new IntegerArithmeticClient(managedChannel);
  }

  public void close() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public CalculationResponse makeCalculation(CalculationRequest request) {
    return blockingStub.makeCalculation(request);
  }
}
