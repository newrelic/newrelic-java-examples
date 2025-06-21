package org.example;

import com.newrelic.api.agent.Trace;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class Main {
    /*
     * Either set -Dotel.java.global-autoconfigure.enabled=true or initialize the OTel
     * SDK by uncommenting the following line to enable reporting of dimensional metrics.
     */
//    Public static final OpenTelemetry openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    public static void main(String[] args) {
        for (int i = 1; i <= 300; i++) {
            try {
                System.out.println("Generating OpenTelemetry Dimensional Metrics - Iteration " + i);
                // Start a New Relic transaction and generate OTel metrics
                generateOTelMetrics();
                // Sleep for a second to keep JVM alive long enough for the agent to harvest data, the demo will run for 5 minutes
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Trace(dispatcher = true)
    public static void generateOTelMetrics() {
        // Generate LongCounter dimensional metrics
        LongCounter longCounter = GlobalOpenTelemetry.get().getMeterProvider().get("otel.demo").counterBuilder("otel.demo.longcounter").build();
        longCounter.add(1, Attributes.of(AttributeKey.stringKey("LongCounter"), "foo"));

        // Generate DoubleHistogram dimensional metrics
        DoubleHistogram doubleHistogram = GlobalOpenTelemetry.get().getMeterProvider().get("otel.demo").histogramBuilder("otel.demo.histogram").build();
        doubleHistogram.record(3, Attributes.of(AttributeKey.stringKey("DoubleHistogram"), "foo"));

        // Generate DoubleGauge dimensional metrics
        DoubleGauge doubleGauge = GlobalOpenTelemetry.get().getMeterProvider().get("otel.demo").gaugeBuilder("otel.demo.gauge").build();
        doubleGauge.set(5, Attributes.of(AttributeKey.stringKey("DoubleGauge"), "foo"));

        // Generate LongUpDownCounter dimensional metrics
        LongUpDownCounter longUpDownCounter = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("otel.demo")
                .upDownCounterBuilder("otel.demo.updowncounter")
                .build();
        longUpDownCounter.add(7, Attributes.of(AttributeKey.stringKey("LongUpDownCounter"), "foo"));
    }
}