package nl.hayovanloon.grpc.simpleserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Logger;


public class IntegerArithmeticServer {

  static final int DEFAULT_PORT = 8081;
  private static final Logger LOG =
      Logger.getLogger(IntegerArithmeticServer.class.getName());
  private final int port;
  private Server server;

  private IntegerArithmeticServer(int port) {
    this.port = port;
  }

  static IntegerArithmeticServer defaultInstance() {
    return new IntegerArithmeticServer(DEFAULT_PORT);
  }

  static IntegerArithmeticServer of(int port) {
    return new IntegerArithmeticServer(port);
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args)
      throws IOException, InterruptedException {

    final IntegerArithmeticServer server;
    if (args.length > 1) {
      server = IntegerArithmeticServer.of(Integer.valueOf(args[0]));
    } else {
      server = IntegerArithmeticServer.defaultInstance();
    }

    server.start();
    server.blockUntilShutdown();
  }

  private void start() throws IOException {
    server = ServerBuilder.forPort(port)
        .addService(new IntegerArithmeticImpl())
        .build()
        .start();
    LOG.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the LOG may have been reset by its JVM shutdown hook.
      System.err
          .println("*** shutting down gRPC server since JVM is shutting down");
      IntegerArithmeticServer.this.stop();
      System.err.println("*** server shut down");
    }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon
   * threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }


  static class IntegerArithmeticImpl
      extends IntegerArithmeticGrpc.IntegerArithmeticImplBase {

    /**
     * Calculates the result of an operation.
     * <p>
     * To keep the implementation readable (this being just a basic
     * demonstration), long primitives were used (rather that BigIntegers). As a
     * consequence, calculation of intermediate results might cause overflow
     * exceptions.
     *
     * @param operation operation to evaluate
     * @return a simple value IntExpression
     */
    static IntExpression calculate(IntOperation operation) {
      final long result;
      final IntOperation.Type type = operation.getType();

      if (type == null) {
        throw new IllegalArgumentException("no type specified on " + operation);
      }

      switch (type) {
        case ADDITION:
          result = add(eval(operation.getOp1()), eval(operation.getOp2()));
          break;
        case SUBTRACTION:
          result = subtract(eval(operation.getOp1()), eval(operation.getOp2()));
          break;
        case MULTIPLICATION:
          result = multiply(eval(operation.getOp1()), eval(operation.getOp2()));
          break;
        case DIVISION:
          result = divide(eval(operation.getOp1()), eval(operation.getOp2()));
          break;
        case MODULO:
          result = modulo(eval(operation.getOp1()), eval(operation.getOp2()));
          break;
        case POWER:
          result = power(eval(operation.getOp1()), eval(operation.getOp2()));
          break;
        default:
          throw new UnsupportedOperationException(operation.getType().name());
      }

      return IntExpression.newBuilder().setNumber(result).build();
    }

    /**
     * Evaluates an IntExpression to a long value.
     *
     * @param expr expression to evaluate
     * @return the evaluated value
     */
    static long eval(IntExpression expr) {
      return expr.hasOperation()
          ? calculate(expr.getOperation()).getNumber() : expr.getNumber();
    }

    static long add(long op1, long op2) {
      return op1 + op2;
    }

    static long subtract(long op1, long op2) {
      return op1 - op2;
    }

    static long multiply(long op1, long op2) {
      return op1 * op2;
    }

    static long divide(long op1, long op2) {
      return op1 / op2;
    }

    static long modulo(long op1, long op2) {
      return op1 % op2;
    }

    /**
     * Performs power calculation.
     *
     * @param op1 first operand
     * @param op2 second operand
     * @return the resulting value
     * @throws IllegalArgumentException if the second operand is negative or
     *                                  greater than {@link Integer#MAX_VALUE}.
     */
    static long power(long op1, long op2) {
      if (op2 < 0 || Integer.MAX_VALUE < op2) {
        final String message = String.format("op2 must be between 0 and %s",
            Integer.MAX_VALUE);
        throw new IllegalArgumentException(message);
      }

      return BigInteger.valueOf(op1).pow((int) op2).longValue();
    }

    @Override
    public void makeCalculation(CalculationRequest request,
                                StreamObserver<CalculationResponse> responseObserver) {

      LOG.info("received a request");

      final CalculationResponse.Builder resp = CalculationResponse.newBuilder()
          .setRequest(request);

      if (request.getOperation() == null) {
        resp
            .addErrors(Error.newBuilder()
                .setCode(3)
                .setMessage("no operation specified"));
      } else {
        try {
          final IntExpression outcome = calculate(request.getOperation());
          resp
              .setResult(CalculationResult.newBuilder()
                  .setExpression(outcome));
          LOG.info("calculated a result: " + outcome);
        } catch (ArithmeticException e) {
          LOG.info("ArithmeticException occurred: " + e.getMessage());
          resp
              .addErrors(Error.newBuilder()
                  .setCode(1)
                  .setMessage(e.getMessage()));
        } catch (IllegalArgumentException e) {
          resp
              .addErrors(Error.newBuilder()
                  .setCode(2)
                  .setMessage(e.getMessage()));
        }
      }

      responseObserver.onNext(resp.build());
      responseObserver.onCompleted();
    }
  }
}
