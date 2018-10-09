package nl.hayovanloon.grpc.simpleserver;

import io.grpc.StatusRuntimeException;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientMain {
  private static final Logger LOG = Logger.getLogger(
      IntegerArithmeticClient.class.getName());

  public static void main(String[] args) throws Exception {
    final int port;
    if (args.length > 1) {
      port = Integer.valueOf(args[0]);
    } else {
      port = IntegerArithmeticServer.DEFAULT_PORT;
    }

    final String host;
    if (args.length > 2) {
      host = args[1];
    } else {
      host = "localhost";
    }

    try (IntegerArithmeticClient client = IntegerArithmeticClient
        .of(host, port)) {

      final CalculationRequest request = createRequest();

      final CalculationResponse response = client.makeCalculation(request);

      LOG.info(response.toString());
    } catch (StatusRuntimeException e) {
      LOG.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
    }
  }

  private static CalculationRequest createRequest() {
    // 6 + 3 ^ (5 % (6 / 2)) = 15
    return CalculationRequest.newBuilder()
        .setOperation(IntOperation.newBuilder()
            .setType(IntOperation.Type.ADDITION)
            .setOp1(IntExpression.newBuilder()
                .setOperation(IntOperation.newBuilder()
                    .setType(IntOperation.Type.MULTIPLICATION)
                    .setOp1(IntExpression.newBuilder()
                        .setNumber(3))
                    .setOp2(IntExpression.newBuilder()
                        .setNumber(2))))
            .setOp2(IntExpression.newBuilder()
                .setOperation(IntOperation.newBuilder()
                    .setType(IntOperation.Type.POWER)
                    .setOp1(IntExpression.newBuilder()
                        .setNumber(3))
                    .setOp2(IntExpression.newBuilder()
                        .setOperation(IntOperation.newBuilder()
                            .setType(IntOperation.Type.MODULO)
                            .setOp1(IntExpression.newBuilder()
                                .setNumber(5))
                            .setOp2(IntExpression.newBuilder()
                                .setOperation(IntOperation.newBuilder()
                                    .setType(IntOperation.Type.DIVISION)
                                    .setOp1(IntExpression.newBuilder()
                                        .setNumber(6))
                                    .setOp2(IntExpression.newBuilder()
                                        .setNumber(2)))))))))
        .build();
  }
}
