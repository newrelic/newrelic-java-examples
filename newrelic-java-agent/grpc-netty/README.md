## grpc-netty Example: Validating `support-grpc-netty`

Self-driving Java app (one JVM acting as both a grpc-netty server and client) for verifying
[GH-2044 "Add support for grpc-netty"](https://github.com/newrelic/newrelic-java-agent/issues/2044).
It fixes two bugs in the generic `netty-4.1.16` instrumentation module's interop with grpc-netty:

- **Bug A**: header lookups on grpc-netty's `Http2Headers` implementations (`Http2RequestHeaderWrapper.getHeader()`)
  silently failed due to a class-name check that could never match.
- **Bug B**: a JVM acting as a grpc-netty client could have its own response headers misread as a
  new inbound request, spawning an extra web transaction.

Bug B's fix uses two layered guards, since the first one alone turned out not to be:
1. `NettyUtil.isRequestHeaders()`: inspects the outbound `Http2Headers` object's own
   `:method`/`:authority` pseudo-headers to guess whether a write is a request.
2. `NettyUtil.isServerConnection()`: reads `Http2ConnectionHandler.connection().isServer()`, a
   plain Netty-level signal that's stable across Netty and grpc-netty versions, since it doesn't
   depend on introspecting any particular header-implementation's capabilities at all. 

Response codes look identical whether these bugs are present, both failure modes are
silent.

This app deliberately disables the separate `grpc-1.4.0`/`1.22.0`/`1.30.0`/`1.40.0`
instrumentation modules (`class_transformer` block in `newrelic/newrelic.yml`). Bug A and Bug B
live in the generic `netty-4.1.16` module, which only ever runs against grpc-netty traffic
when `netty.http2.frame_read_listener.start_transaction: true` is set. With that flag on, the separate
`grpc-1.40.0` module's own `ServerTransportListenerImpl_Instrumentation.wrapMethod`, which runs on
a different thread than the one that read the HTTP/2 HEADERS frame (grpc jumps RPC execution to
its own executor), starts a second, independent transaction for the same call, since
`@Trace(dispatcher=true)` finds no active transaction on that thread. Thia is transaction duplication, but it's an artifact of combining this opt-in flag with grpc's
normal instrumentation, not a `support-grpc-netty` defect (the flag defaults to `false` specifically to avoid this). Disabling the
grpc-* modules here keeps every transaction this app produces attributable to the netty-4.1.16
module alone, so the counts are simple.

## Module coverage map

| Traffic | Exercises |
|---|---|
| Unary `SayHello` (every tick) | **Bug A** fix (header capture on grpc-netty's `Http2Headers` via `Http2RequestHeaderWrapper`) |
| Bidi `SayHelloStreaming` (every tick) | Bug A fix, on a bidi-streaming call |
| Deliberate `ThrowException` (every 3rd tick) | Bug A fix, on a call whose server handler throws |
| The client's own outbound calls, continuously | **Bug B** fix (`isServerConnection()` + `sawOutboundRequestHeaders` guards preventing spurious client-side transactions) |

grpc-level features (DT header injection, error capture, transaction naming via
`ClientCall_Instrumentation`/`ServerImpl_Instrumentation`) are provided by the separate
`grpc-1.40.0` module, which this app disables, those are validated by the agent's own
`instrumentation/grpc-1.40.0` test suite, not by this app.

## Prerequisites

- JDK 17 or later
- A New Relic license key

## Setup

### 1. Agent JAR

```bash
cp /path/to/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar newrelic/newrelic.jar
```

### 2. License key

Edit `newrelic/newrelic.yml` and replace `INSERT_YOUR_LICENSE_KEY_HERE` with your key.

## Running

Pass `-PagentJar=…` to use a JAR directly from the agent build output (skipping the copy step).
`-Pdebug` additionally enables `-Dnewrelic.debug=true`, useful for the validation steps below.

```bash
./gradlew :run -Pagent -Pdebug \
  -PagentJar=/path/to/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar \
  -PagentConfig=/path/to/newrelic-java-examples/newrelic-java-agent/grpc-netty/newrelic/newrelic.yml
```

Runs indefinitely: a unary + a streaming call every 5s, plus a deliberate error every 3rd tick.
Ctrl-C to stop.

