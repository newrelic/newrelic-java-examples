# Micronaut Example Application

Primarily aims to reproduce [newrelic-java-agent#3038](https://github.com/newrelic/newrelic-java-agent/issues/3038) via agent version 9.4.0:
a token handoff across an async thread hop in the agent's Netty instrumentation wasn't always
relinked, causing it to die too early. This application can also be used as a starting point for working with Micronaut instrumentation.

## #3038: custom attributes and transaction naming lost

**Symptom:** custom attributes and transaction naming set from a Micronaut `@ServerFilter` are
silently dropped, and the agent records extra "phantom" `Transaction` events bound to request
path `/` instead of the real URI. Triggered whenever a request body arrives across more than one
Netty read, Micronaut dispatches to the router/filters from inside `channelReadComplete()` instead of `channelRead()`, and the agent's token wasn't relinked for that
path.

On `newrelic-java-agent` branch `resolve-micronaut-custom-attributes-issue`,
two changes were made to verify resolution of behavior:
- `PipeliningServerHandler_Instrumentation` (all affected `micronaut-http-server-netty-*`
  versions): reordered so the original method runs before the token is expired, not after.
- `PropagatedContext_Instrumentation` (`micronaut-core-4.0.0` and `-4.3.0`): relinks the token
  across the thread hop Micronaut's `@ExecuteOn` support uses internally, which had the same
  no-relink gap.

### Reproducing

```bash
SMALL_BODY=$(python3 -c "print('x'*20)")
LARGE_BODY=$(python3 -c "print('x'*5000)")

for i in $(seq 1 20); do
  curl -s -o /dev/null -w "%{http_code}\n" -X PUT -H "Content-Type: text/plain" \
    -d "$SMALL_BODY" "http://localhost:8080/acme/1/resource-b/$i/action"
done | sort | uniq -c   # expect all 204, no drops

for i in $(seq 1 20); do
  curl -s -o /dev/null -w "%{http_code}\n" -X PUT -H "Content-Type: text/plain" \
    -d "$LARGE_BODY" "http://localhost:8080/acme/1/resource-b/$i/action"
done | sort | uniq -c   # expect all 204 too
```

Then check the agent log:

```bash
# Should be 0 with the fix in place (was 100% pre-fix for chunked bodies).
grep -c 'Unable to add custom attribute' newrelic/logs/newrelic_agent.log

# Phantom "/"-bound transactions — expect roughly one per chunked request, zero for small-body ones.
grep -c 'for request: / finished' newrelic/logs/newrelic_agent.log
```

If you see HTTP `415` instead of `204`, check the `Content-Type: text/plain` header is present.

## Prerequisites

- JDK 17 or later (SDKMAN: `sdk use java 17.0.x-tem`)
- New Relic Java Agent JAR and license key

## Setup

```bash
cp /path/to/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar newrelic/newrelic.jar
```

Edit `newrelic/newrelic.yml` and replace the license key placeholder with your own.

## Running

```bash
./gradlew :run -Pagent \
  -PagentJar=/path/to/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar \
  -PagentConfig=/path/to/newrelic-java-examples/newrelic-java-agent/micronaut/newrelic/newrelic.yml
```

`-PagentJar`/`-PagentConfig` are optional — omit them to use the jar/config already checked into
`newrelic/`.

## Routes

| Route | Method | Purpose |
|---|---|---|
| `/{parent}/{parentId}/resource-a/{resourceAId}` | GET | Baseline route |
| `/{parent}/{parentId}/resource-b/{resourceBId}` | GET | Second resource family |
| `/{parent}/{parentId}/resource-b/{resourceBId}/action` | PUT, body | Send a small vs. large body to toggle the bug |
| `/{parent}/{parentId}/resource-b/{action}` | POST, body | Same body-size toggle |
| `/{parent}/{parentId}/resource-a/{resourceAId}/reactive` | GET | `Mono` hopping to `Schedulers.boundedElastic()` from a controller |
| `/chain` | GET | Outbound call via `micronaut-http-client` back into this same server |

Every route under `ResourceController` goes through `CustomAttributeFilter` (request + response
phases) and runs on `@ExecuteOn(TaskExecutors.IO)`.
