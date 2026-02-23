package org.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import org.example.otel.apis.LogsAPI;
import org.example.otel.apis.MetricsAPI;
import org.example.otel.apis.TraceAPI;

public class Main {
    /*
     * The opentelemetry-sdk-extension-autoconfigure dependency needs to be initialized
     * by either setting -Dotel.java.global-autoconfigure.enabled=true or calling the
     * following API in order for the New Relic Java agent instrumentation to load.
     */
    private static final OpenTelemetry OPEN_TELEMETRY_SDK = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    public static void main(String[] args) {
        TraceAPI traceAPI = new TraceAPI(OPEN_TELEMETRY_SDK);
        LogsAPI logsAPI = new LogsAPI();
        MetricsAPI metricsAPI = new MetricsAPI();

        for (int i = 1; i <= 6000; i++) {
            try {
                traceAPI.generateOtelSpans();
                logsAPI.generateOTelLogs(i);
                metricsAPI.generateOTelMetrics();
                // Sleep for a second to keep JVM alive long enough for the
                // Java agent to harvest data. The demo will run for 5 minutes.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
