package nl.hayovanloon.grpc.simpleserver;

import static org.junit.Assert.assertEquals;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import nl.hayovanloon.grpc.simpleserver.IntegerArithmeticGrpc.IntegerArithmeticBlockingStub;
import nl.hayovanloon.grpc.simpleserver.IntegerArithmeticServer.IntegerArithmeticImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IntegerArithmeticServerTest {

  private IntegerArithmeticBlockingStub blockingStub;

  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  @Before
  public void setUp() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName).directExecutor().addService(new IntegerArithmeticImpl()).build().start());

    blockingStub = IntegerArithmeticGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

  }

  @Test
  public void makeCalculation_happy() {
    CalculationRequest request = CalculationRequest.newBuilder()
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

    CalculationResponse response = blockingStub.makeCalculation(request);

    assertEquals(15, response.getResult().getExpression().getNumber());
  }

  /**
   * Without a type nothing can be done, so an error (code 2) is expected.
   */
  @Test
  public void makeCalculation_missingType() {
    CalculationRequest request = CalculationRequest.newBuilder()
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
                            //.setType(IntOperation.Type.MODULO)  <-- here
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

    CalculationResponse response = blockingStub.makeCalculation(request);

    assertEquals(2, response.getErrors(0).getCode());
  }

  /**
   * Missing operands default to zero. So no errors, just a different result.
   */
  @Test
  public void makeCalculation_missingOperand() {
    CalculationRequest request = CalculationRequest.newBuilder()
        .setOperation(IntOperation.newBuilder()
            .setType(IntOperation.Type.ADDITION)
            .setOp1(IntExpression.newBuilder()
                .setOperation(IntOperation.newBuilder()
                    .setType(IntOperation.Type.MULTIPLICATION)
                    //.setOp1(IntExpression.newBuilder()  <-- here
                    //    .setNumber(3))
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

    CalculationResponse response = blockingStub.makeCalculation(request);

    assertEquals(9, response.getResult().getExpression().getNumber());
  }

  /**
   * Division by zero -> ArithmeticException. So an error (code 1) is expected.
   */
  @Test
  public void makeCalculation_divisionByZero() {
    CalculationRequest request = CalculationRequest.newBuilder()
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
                                        .setNumber(0)))))))))
        .build();

    CalculationResponse response = blockingStub.makeCalculation(request);

    assertEquals(1, response.getErrors(0).getCode());
  }
}
