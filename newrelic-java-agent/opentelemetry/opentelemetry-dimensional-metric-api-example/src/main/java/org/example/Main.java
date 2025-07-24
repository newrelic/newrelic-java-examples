package org.example;

import com.newrelic.api.agent.Trace;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class Main {
    /*
     * Either set -Dotel.java.global-autoconfigure.enabled=true or initialize the OTel
     * SDK by uncommenting the following line to enable reporting of dimensional metrics.
     */
//    Public static final OpenTelemetry openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    public static void main(String[] args) {
        createAsyncMeters();

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
        longCounter.add(1, Attributes.of(AttributeKey.stringKey("LongCounter"), "foo", AttributeKey.longKey("attr.sample"), 100L));

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

    private static void createAsyncMeters() {
        // Async long counter
        ObservableLongCounter asyncLongCounter =
                GlobalOpenTelemetry.get().getMeterProvider().get("otel.demo")
                        .counterBuilder("otel.async.demo.longcounter")
                        .setUnit("things")
                        .buildWithCallback(
                                // the callback is invoked when a MetricReader reads metrics
                                observableMeasurement -> {
                                    observableMeasurement.record(2000, Attributes.of(AttributeKey.stringKey("AsyncLongCounter"), "YES"));
                                    System.out.println("AsyncLongCounter record");
                                });

        // Async up/down counter
        ObservableLongUpDownCounter asyncUpDownCounter =
                GlobalOpenTelemetry.get().getMeterProvider().get("otel.demo")
                        .upDownCounterBuilder("otel.async.demo.longupdowncounter")
                        .setUnit("things")
                        .buildWithCallback(
                                // the callback is invoked when a MetricReader reads metrics
                                observableMeasurement -> {
                                    observableMeasurement.record(2000, Attributes.of(AttributeKey.stringKey("AsyncLongUpDownCounter"), "YES"));
                                    System.out.println("AsyncLongUpDownCounter record");
                                });

        // Async gauge counter
        ObservableDoubleGauge asyncDoubleGauge =
                GlobalOpenTelemetry.get().getMeterProvider().get("otel.demo")
                        .gaugeBuilder("otel.async.demo.gauge")
                        .setUnit("things")
                        .buildWithCallback(
                                // the callback is invoked when a MetricReader reads metrics
                                observableMeasurement -> {
                                    observableMeasurement.record(2000, Attributes.of(AttributeKey.stringKey("AsyncDoubleGauge"), "YES"));
                                    System.out.println("AsyncDoubleGauge record");
                                });
    }
}