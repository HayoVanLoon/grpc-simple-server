# Simple gRPC Server

A gRPC server with minimal functionality; mainly to be re-used in other 
projects.


## Usage

Start the server.
```bash
make run
```

By default the server will listen on port 8081, but this can be overriden.
```bash
make run PORT=9000
```

Make a test call from a static client.
```bash
make test-call
```

If the server is listening on another port, you will need to specify it.
```bash
make test-call PORT=9000
```

By default it tries to connect to localhost. You can specify a different host.
```bash
make test-call PORT=9000 HOST=otherhost.local
```

Package the server into a JAR
```bash
make dist
```