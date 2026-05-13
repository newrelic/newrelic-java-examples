# Mule 4.9 + New Relic Java Agent Reproduction

Reproduces an `IllegalAccessError` when running Mule 4.9 + JDK 17 with the New Relic Java agent.

## The issue

The NR agent weaves classes in Mule's named JPMS modules (`org.mule.runtime.core`,
`org.mule.runtime.api`, `org.mule.runtime.core.components`) and injects synthetic
`_nr_ext` helper classes into the *unnamed* module. JPMS forbids named → unnamed
module access, so every request through the instrumented execution paths throws:

```
IllegalAccessError: class org.mule.runtime.core.internal.event.AbstractEventContext
(in module org.mule.runtime.core) cannot access class
com.newrelic.weave...AbstractEventContext_xxx_nr_ext (in unnamed module)
because module org.mule.runtime.core does not read unnamed module
```

The `repro-app` flow exercises five instrumented classes to maximise boundary crossings:
`TryScope`, `ChoiceRouter`, `ScatterGatherRouter`, `Foreach/ForeachRouter`, and
`AsynchronousRetryTemplate/RetryWorker` (via a deliberate failed HTTP connection).

## Prerequisites

Place `mule-ee-distribution-standalone-4.9.11.zip` in the `runtime` directory before proceeding.

**JDK 17 is required.** `mule-maven-plugin` validates the JVM version at build time.
If your `JAVA_HOME` is not JDK 17, pass the path explicitly:
```bash
mvn package -Djava.home.17=/path/to/jdk17
```

## Error scenario (baseline agent)

```bash
# Build repro-app, unpack Mule, deploy app, configure wrapper.conf
mvn package

# Start Mule (wait for "Started app 'repro-app...'" before sending traffic)
./runtime/mule-enterprise-standalone-4.9.11/bin/mule

# Send traffic
for i in $(seq 1 10); do
  curl -s http://localhost:8081/repro > /dev/null
  curl -s "http://localhost:8081/repro?mode=fast" > /dev/null
done

# Confirm errors
grep -c "IllegalAccessError" target/newrelic-agent.log
```

With the baseline agent you'll see hundreds of `IllegalAccessError` entries per request,
broken down across `org.mule.runtime.core`, `org.mule.runtime.api`,
`org.mule.runtime.core.components`, `java.logging`, and `com.mulesoft.mule.runtime.core.ee`.

## Fix scenario

```bash
# Stop Mule
./runtime/mule-enterprise-standalone-4.9.11/bin/mule stop

# If the above command takes too long run the following:
pkill -9 -f "mule-enterprise-standalone"

# Rebuild with the fix agent
mvn package -Dnewrelic.agent.jar=$(pwd)/vendor/newrelic/newrelic-fix.jar

# Clear the log so the grep count reflects only this run
rm -f target/newrelic-agent.log

# Start Mule and send the same traffic
./runtime/mule-enterprise-standalone-4.9.11/bin/mule

for i in $(seq 1 10); do
  curl -s http://localhost:8081/repro > /dev/null
  curl -s "http://localhost:8081/repro?mode=fast" > /dev/null
done

# Confirm clean
grep -c "IllegalAccessError" target/newrelic-agent.log   # should be 0
```

# Note: currently the fix variant will cause the instrumentation to hang after making a call, this is being actively working on by Labs.
