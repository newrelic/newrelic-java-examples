## vertx5-web-example

A single-verticle Vert.x 5.0.0 application that exercises the New Relic Java agent's
Vert.x 5 instrumentation. Each HTTP endpoint triggers a specific instrumentation hook so
you can verify coverage in the New Relic UI.

### Prerequisites

- Java 11+ (Vert.x 5.0.0 requires Java 11; set `JAVA_HOME` accordingly)
- Maven (or use the included `./mvnw` wrapper)
- A New Relic account and ingest license key

### Step 1: Configure the license key

Edit `newrelic/newrelic.yml` and replace `<INSERT LICENSE KEY HERE>` with your account's
ingest license key:

```yaml
license_key: 'abc123...'
```

Or pass it as an environment variable at runtime (skip editing the file):

```bash
export NEW_RELIC_LICENSE_KEY=abc123...
```

The `app_name` in `newrelic.yml` is already set to `vertx5-web-example` — this is what
appears in the New Relic UI.

### Step 2: Build

```bash
cd vertx5-web-example
JAVA_HOME=/path/to/jdk11 ./mvnw clean package
```

### Step 3: Run with the agent

```bash
JAVA_HOME=/path/to/jdk11 \
  java -javaagent:newrelic/newrelic.jar \
       -Dnewrelic.config.file=newrelic/newrelic.yml \
       -jar target/vertx5-web-example-1.0.jar
```

The server starts on **http://localhost:8080**. The agent connects to New Relic in the
background and the app shows up under **APM & Services → vertx5-web-example** within
about 60 seconds.

---

### Endpoints with examples

Run these curl commands while the app is running to generate transactions in New Relic:

```bash
# 1. Simple route
curl http://localhost:8080/api/hello

# 2. Parameterised path: :name captured in the transaction name
curl http://localhost:8080/api/greet/Alice

# 3. Two-handler chain: exercises RoutingContextImplBase.iterateNext()
curl http://localhost:8080/api/chain

# 4. Blocking handler: exercises BlockingHandlerDecorator
curl http://localhost:8080/api/blocking/heavy-work

# 5. executeBlocking(Callable): exercises ContextImpl + CallableWrapper
#    (single-arg delegates to the instrumented two-arg form with ordered=true)
curl http://localhost:8080/api/compute

# 6. HTTP client external call: exercises HttpClientImpl + external segment
curl http://localhost:8080/api/fetch

# 7. Sub-router
curl http://localhost:8080/sub/ping

# 8. Intentional error: exercises unhandledFailure + error recording
curl http://localhost:8080/api/fail

# 9. HTTP client DNS failure: exercises handleException + UnknownHostException
#    metric + expireAllTokens() in AsyncRequestResultHandler
curl http://localhost:8080/api/fetch-bad

# 10. Reroute: exercises RoutingContextImplBase.restart()
curl http://localhost:8080/api/reroute

# 11. Header echo: returns inbound request headers (used by /api/fetch-headers)
curl http://localhost:8080/api/echo-headers

# 12. Fetch with DT headers: calls /api/echo-headers; response shows the
#     W3C traceparent / newrelic headers injected by OutboundWrapper
curl http://localhost:8080/api/fetch-headers
```

---

### What to look for on the NR Dashboard

**APM → vertx5-web-example → Transactions**

Each endpoint creates a distinct transaction name:

| Endpoint | Transaction name in NR |
|---|---|
| `/api/hello` | `WebTransaction/Vertx/api/hello (GET)` |
| `/api/greet/Alice` | `WebTransaction/Vertx/api/greet/:name (GET)` |
| `/api/chain` | `WebTransaction/Vertx/api/chain (GET)` |
| `/api/blocking/heavy-work` | `WebTransaction/Vertx/api/blocking/:task (GET)` |
| `/api/compute` | `WebTransaction/Vertx/api/compute (GET)` |
| `/api/fetch` | `WebTransaction/Vertx/api/fetch (GET)` |
| `/sub/ping` | `WebTransaction/Vertx/sub/ping (GET)` |
| `/api/fail` | `WebTransaction/Vertx/api/fail (GET)` |
| `/api/fetch-bad` | `WebTransaction/Vertx/api/fetch-bad (GET)` |
| `/api/reroute` | `WebTransaction/Vertx/api/hello (GET)` (rerouted; name reflects the final path) |
| `/api/echo-headers` | `WebTransaction/Vertx/api/echo-headers (GET)` |
| `/api/fetch-headers` | `WebTransaction/Vertx/api/fetch-headers (GET)` |
