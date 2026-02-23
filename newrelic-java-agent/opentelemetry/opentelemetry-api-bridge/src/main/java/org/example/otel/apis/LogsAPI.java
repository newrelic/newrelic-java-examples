package org.example.otel.apis;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Instant;
import java.util.Arrays;

/**
 * Demonstrates how to generate OpenTelemetry logs using the OpenTelemetry Logs API.
 * The New Relic Java agent automatically picks up and exports these logs to New Relic.
 */
public class LogsAPI {
    // Create an OTel LogRecordExporter to export logs to the console
    private final SystemOutLogRecordExporter systemOutLogRecordExporter = SystemOutLogRecordExporter.create();

    // Initialize OTel LogRecordProcessor
    private final LogRecordProcessor logRecordProcessor = SimpleLogRecordProcessor.create(systemOutLogRecordExporter);

    // Custom attributes for the Resource
    private final Attributes attributes = Attributes.builder()
            .put("service.name", NewRelic.getAgent().getConfig().getValue("app_name", "unknown"))
            .put("service.version", "1.0.0")
            .put("environment", "staging")
            .build();

    // Create OTel Resource with custom attributes
    private final Resource customResource = Resource.create(attributes);

    // Build OTel SdkLoggerProvider
    private final SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder().addLogRecordProcessor(logRecordProcessor).setResource(
            customResource).build();

    // Initialize OTel LoggerBuilder
    private final LoggerBuilder loggerBuilder = sdkLoggerProvider
            .loggerBuilder("opentelemetry-logs-api-demo")
            .setInstrumentationVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.0.0");

    // Build OTel Logger
    private final Logger logger = loggerBuilder.build();

    public LogsAPI() {
    }

    /**
     * Generate OpenTelemetry Logs
     *
     * @param i iteration number
     */
    public void generateOTelLogs(int i) {
        System.out.println("\n ===== Generating OpenTelemetry Logs =====\n");

        // Start a New Relic transaction and generate OTel logs
        logInNRTransaction(i);
        // Generate OTel logs with no transaction
        logNoTransaction(i);
    }

    @Trace(dispatcher = true)
    public void logInNRTransaction(int iteration) {
        logAtDifferentSeverities(iteration);
    }

    public void logNoTransaction(int iteration) {
        logAtDifferentSeverities(iteration);
    }

    @Trace
    public void logAtDifferentSeverities(int iteration) {
        try {
            emitOTelLogs(iteration, Severity.TRACE);
            Thread.sleep(1000);
            emitOTelLogs(iteration, Severity.INFO);
            Thread.sleep(1000);
            emitOTelLogs(iteration, Severity.DEBUG);
            Thread.sleep(1000);
            emitOTelLogs(iteration, Severity.WARN);
            Thread.sleep(1000);
            emitOTelLogs(iteration, Severity.ERROR);
            Thread.sleep(1000);
            emitOTelLogs(iteration, Severity.FATAL);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void emitOTelLogs(int iteration, Severity severity) {
        // create LogRecordBuilder
        LogRecordBuilder logRecordBuilder = logger.logRecordBuilder();

        Instant now = Instant.now();
        logRecordBuilder
//                .setContext()
                .setBody("Generating OpenTelemetry LogRecord - Iteration " + iteration)
                .setSeverity(severity)
                .setSeverityText("This is severity text")
                .setAttribute(AttributeKey.stringKey("foo"), "bar")
                .setObservedTimestamp(now)
                .setObservedTimestamp(now.toEpochMilli(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .setTimestamp(now)
                .setTimestamp(now.toEpochMilli(), java.util.concurrent.TimeUnit.MILLISECONDS)
//                .setEventName("OpenTelemetry Log Record")
        ;

        // If severity is ERROR, add exception attributes
        if (severity == Severity.ERROR) {
            try {
                throw new RuntimeException("This is a test exception for severity ERROR");
            } catch (RuntimeException e) {
                logRecordBuilder.setAttribute(AttributeKey.stringKey("exception.message"), e.getMessage());
                logRecordBuilder.setAttribute(AttributeKey.stringKey("exception.type"), e.getClass().getName());
                logRecordBuilder.setAttribute(AttributeKey.stringKey("exception.stacktrace"), Arrays.toString(e.getStackTrace()));
            }
        }

        logRecordBuilder.emit();
    }
}
