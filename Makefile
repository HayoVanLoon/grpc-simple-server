
SERVER_CLASS = nl.hayovanloon.grpc.simpleserver.IntegerArithmeticServer
CLIENT_CLASS = nl.hayovanloon.grpc.simpleserver.ClientMain
PORT = 8081
HOST = localhost

.PHONY: clean

clean:
	mvn clean

build:
	mvn clean compile

test:
	mvn clean verify

run: build
	mvn exec:java -Dexec.mainClass=$(SERVER_CLASS) -Dexec.args="$(PORT)"

test-call: build
	mvn exec:java -Dexec.mainClass=$(CLIENT_CLASS) -Dexec.args="$(PORT) $(HOST)"

dist: clean
	mvn assembly:single
