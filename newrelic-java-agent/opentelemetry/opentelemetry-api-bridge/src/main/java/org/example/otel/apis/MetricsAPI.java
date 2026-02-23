package org.example.otel.apis;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongCounter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Demonstrates how to generate OpenTelemetry dimensional metrics using the OpenTelemetry Metrics API.
 * The New Relic Java agent automatically configures the OTel SDK to export these metrics to New Relic.
 */
public class MetricsAPI {
    private static final Queue<Long> JOB_QUEUE = new ConcurrentLinkedQueue<>();

    public MetricsAPI() {
    }

    /**
     * Generate OpenTelemetry Dimensional Metrics
     */
    public void generateOTelMetrics() {
        System.out.println("\n ===== Generating OpenTelemetry Dimensional Metrics =====\n");

        // Generate ObservableLongCounter dimensional metrics
        Consumer<ObservableLongMeasurement> observableCounterCallback = measurement -> measurement.record(JOB_QUEUE.size(),
                Attributes.of(AttributeKey.stringKey("ObservableLongCounter"), "foo"));

        ObservableLongCounter observableCounter = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("opentelemetry-metrics-api-demo")
                .counterBuilder("opentelemetry-metrics-api-demo.observablecounter")
                .setDescription("Total number of jobs processed")
                .setUnit("jobs")
                .buildWithCallback(observableCounterCallback);

        System.out.println("Created ObservableLongCounter metric: " + observableCounter);

        // Generate ObservableDoubleGauge dimensional metrics
        Consumer<ObservableDoubleMeasurement> observableGaugeCallback = measurement -> measurement.record(JOB_QUEUE.size(),
                Attributes.of(AttributeKey.stringKey("ObservableDoubleGauge"), "foo"));

        ObservableDoubleGauge observableGauge = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("opentelemetry-metrics-api-demo")
                .gaugeBuilder("opentelemetry-metrics-api-demo.observablegauge")
                .setDescription("Total number of jobs processed")
                .setUnit("jobs")
                .buildWithCallback(observableGaugeCallback);

        System.out.println("Created ObservableDoubleGauge metric: " + observableGauge);

        // Simulate adding jobs to the queue for observable measurement callbacks
        for (int i = 0; i < 5; i++) {
            try {
                JOB_QUEUE.add((long) i);
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Added job to queue, current size: " + JOB_QUEUE.size());
        }

        // Generate LongCounter dimensional metrics
        LongCounter longCounter = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("opentelemetry-metrics-api-demo")
                .counterBuilder("opentelemetry-metrics-api-demo.longcounter")
                .build();
        longCounter.add(1, Attributes.of(AttributeKey.stringKey("LongCounter"), "foo"));

        System.out.println("Created LongCounter metric: " + longCounter);

        // Generate DoubleHistogram dimensional metrics
        DoubleHistogram doubleHistogram = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("opentelemetry-metrics-api-demo")
                .histogramBuilder("opentelemetry-metrics-api-demo.histogram")
                .build();
        doubleHistogram.record(3, Attributes.of(AttributeKey.stringKey("DoubleHistogram"), "foo"));

        System.out.println("Created DoubleHistogram metric: " + doubleHistogram);

        // Generate DoubleGauge dimensional metrics
        DoubleGauge doubleGauge = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("opentelemetry-metrics-api-demo")
                .gaugeBuilder("opentelemetry-metrics-api-demo.gauge")
                .build();
        doubleGauge.set(5, Attributes.of(AttributeKey.stringKey("DoubleGauge"), "foo"));

        System.out.println("Created DoubleGauge metric: " + doubleGauge);

        // Generate LongUpDownCounter dimensional metrics
        LongUpDownCounter longUpDownCounter = GlobalOpenTelemetry.get()
                .getMeterProvider()
                .get("opentelemetry-metrics-api-demo")
                .upDownCounterBuilder("opentelemetry-metrics-api-demo.updowncounter")
                .build();
        longUpDownCounter.add(7, Attributes.of(AttributeKey.stringKey("LongUpDownCounter"), "foo"));

        System.out.println("Created LongUpDownCounter metric: " + longUpDownCounter);
    }
}
