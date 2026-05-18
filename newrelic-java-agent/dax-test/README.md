# AWS DAX (DynamoDB Accelerator)

Example application demonstrating New Relic Java agent instrumentation with Amazon DynamoDB and DAX (DynamoDB Accelerator). This app exercises both synchronous and asynchronous DynamoDB/DAX clients with various operations including GetItem, Query, Scan, and BatchGetItem.

## Requirements

### AWS

This application requires AWS credentials configured in your environment to access DynamoDB. See the [AWS SDK documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html) for credential configuration options.

To use DAX caching, you'll need a running DAX cluster. See the [DAX Getting Started Guide](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DAX.client.html) for setup instructions.

### Java Agent

The New Relic Java agent jar must be attached to the application via the `-javaagent` JVM arg:

```
-javaagent:/path/to/newrelic.jar
```

Make sure you have a valid `newrelic.yml` config file (or associated system properties/environment variables) to properly configure the agent.

## Build and Run

From the project root, create an executable jar:

```commandline
./gradlew shadowJar
```

Run with DynamoDB only (no DAX):

```commandline
java -javaagent:/path/to/newrelic.jar -jar build/libs/dax-test-1.0-SNAPSHOT-all.jar
```

Run with DAX caching enabled (pass the DAX cluster URL as an argument):

```commandline
java -javaagent:/path/to/newrelic.jar -jar build/libs/dax-test-1.0-SNAPSHOT-all.jar dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com
```

## What the Demo Does

The application performs the following operations:

1. Creates a test table (`TryDaxTable`) in DynamoDB
2. Populates the table with test data
3. Runs read operations using both async and sync clients:
   - `Scan` - Full table scan
   - `GetItem` - Individual item retrieval
   - `Query` - Range queries with sort key conditions
   - `BatchGetItem` - Batch retrieval of multiple items
4. Deletes the test table

When a DAX URL is provided, read operations go through the DAX cache, demonstrating cache miss on first iteration and cache hits on subsequent iterations.

## Viewing Traces in New Relic

The application uses New Relic's `@Trace` annotations to create distributed traces. After running the demo, you can view the traces in New Relic's Distributed Tracing UI to see the instrumented DynamoDB/DAX operations.
