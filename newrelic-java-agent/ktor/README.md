# Ktor Example: New Relic Java Agent Instrumentation

This app exercises the Ktor 3.x instrumentation modules in the New Relic Java Agent.

## Module coverage map

| Instrumentation module | Covered by |
|---|---|
| `ktor-utils-3.0` | Every inbound request (pipeline token propagation) |
| `ktor-client-core-3.0` | CIO client calls in `/chain`, `/async-deep` |
| `ktor-server-core-3.0.0` | Main Netty server |
| `ktor-server-netty-3.0.0` | Netty engine on port 8080 |

## Prerequisites

- JDK 17 or later (SDKMAN: `sdk use java 17.0.x-tem`)
- New Relic Java Agent JAR and license key

## Setup

### 1. Agent JAR

```bash
cp /path/to/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar newrelic/newrelic.jar
```

### 2. License key

Edit `newrelic/newrelic.yml` and replace `YOUR_LICENSE_KEY_HERE` with your key.

## Running

Pass `-PagentJar=…` to use a JAR directly from the agent build output (skipping the copy step):

```bash
./gradlew :run -Pagent \
  -PagentJar=/path/to/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar \
  -PagentConfig=/path/to/newrelic-java-examples/newrelic-java-agent/ktor/newrelic/newrelic.yml
```

## Routes

| Route | What it validates |
|---|---|
| `GET /hello` | Server pipeline, transaction naming |
| `GET /greet/{name}` | Path-param naming (`GET /greet/{name}`, not `/greet/Alice`) |
| `POST /echo` | Receive/respond pipeline |
| `GET /json` | Content-negotiation through pipeline |
| `GET /slow` | Token survives `delay(300ms)` coroutine suspension |
| `GET /error` | Error collector (HTTP 500) |
| `GET /chain` | Outbound DT via CIO HTTP client → same Netty server; inbound + outbound spans |
| `GET /async-deep` | Token survives two sequential `delay()` yields + an outbound client call |

## Exercising the routes

```bash
for route in /hello "/greet/Alice" /json /slow /chain /async-deep; do
  curl -s "http://localhost:8080$route" > /dev/null
done
curl -s -o /dev/null http://localhost:8080/error

# Continuous load (keeps a steady stream of transactions flowing to NR)
while true; do
  curl -s http://localhost:8080/chain > /dev/null
  curl -s http://localhost:8080/async-deep > /dev/null
  sleep 1
done
```
