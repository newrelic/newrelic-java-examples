package org.example.otel.apis;

import com.newrelic.api.agent.Trace;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to generate OpenTelemetry Spans using the OpenTelemetry Trace API.
 * The New Relic Java agent automatically picks up and exports these Spans to New Relic.
 */
public class TraceAPI {
    private final Tracer tracer;

    public TraceAPI(OpenTelemetry openTelemetrySdk) {
        this.tracer = openTelemetrySdk.getTracer("opentelemetry-trace-api-demo", "1.0.0");
    }

    /**
     * Generates OpenTelemetry spans.
     * <p>
     * Depending on the SpanKind, and whether a New Relic transaction is present, different outcomes
     * can be expected as far as how the New Relic Java agent handles the OpenTelemetry spans.
     */
    public void generateOtelSpans() throws InterruptedException {
        System.out.println("\n ===== Generating OpenTelemetry Data =====\n");

        // create span links
        createSpanLinks();

        // create span events
        createSpanEvents();

        // no txn started on its own
        noSpanKind();

        // calls noSpanKind() but wraps it in a NR txn
        nrTraceNoSpanKind();

        // For client spans, we either turn it into a database span or an external based on the span attributes. No txn started on its own
        clientSpanKind();
        dbClientSpanKind();
        externalClientSpanKind();

        // calls clientSpanKind() but wraps it in a NR txn
        nrTraceClientSpanKind();

        // starts an OtherTransaction txn based on consumer span kind
        consumerSpanKind();

        // no txn started on its own
        producerSpanKind();

        // calls producerSpanKind() but wraps it in a NR txn
        nrTraceProducerSpanKind();

        // starts a WebTransaction/Uri txn based on server span kind
        serverSpanKind();
    }

    @Trace(dispatcher = true)
    public void createSpanEvents() {
        System.out.println("Called createSpanEvents");
        Span span = tracer.spanBuilder("spanWithEvents").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span
                    .addEvent("event1")
                    .addEvent("event2", Instant.ofEpochSecond(System.nanoTime()))
                    .addEvent("event3", Attributes.builder().put("foo", "bar").build())
                    .addEvent("event4", Attributes.builder().put("bar", "baz").build(), System.nanoTime(), TimeUnit.NANOSECONDS)
                    .addEvent("event5", Attributes.builder().put("baz", "buz").build(), Instant.ofEpochSecond(System.nanoTime()))
                    .addEvent("event6", System.nanoTime(), TimeUnit.NANOSECONDS);
            Thread.sleep(1000);
            throw new RuntimeException("Exception in createSpanEventException");
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, "Welp... we've got an error in createSpanEvents");

            span.recordException(t);
            span.recordException(t, Attributes.builder()
                    .put("exception.message", t.getMessage())
                    .put("exception.type", t.getClass().getName())
                    .put("exception.stacktrace", Arrays.toString(t.getStackTrace()))
                    .build());
        } finally {
            span.end();
        }
    }

    public void createSpanLinks() {
        System.out.println("Called createSpanLinks");
        try {
            SpanContext spanContext = upstreamSpan();
            for (int i = 0; i < 20; i++) {
                downstreamSpan(spanContext, i);
            }
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    // create an upstream span and return its SpanContext
    @Trace(dispatcher = true)
    public SpanContext upstreamSpan() throws InterruptedException {
        Span span = tracer.spanBuilder("upstreamSpan").startSpan();
        SpanContext spanContext;
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called upstreamSpan");
            spanContext = span.getSpanContext();
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
        return spanContext;
    }

    // create a downstream span with a link to the SpanContext of an upstream span
    @Trace(dispatcher = true)
    public void downstreamSpan(SpanContext spanContext, int iteration) throws InterruptedException {
        Span span = tracer.spanBuilder("downstreamSpan")
                .addLink(spanContext, Attributes.builder().put("iteration", iteration).put("customLinkAttribute", "someValue").build())
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called downstreamSpan");
            span.setStatus(StatusCode.OK, "All is good in the downstreamSpan");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    @Trace(dispatcher = true)
    public void nrTraceNoSpanKind() {
        System.out.println("Called nrTraceNoSpanKind");
        try {
            noSpanKind();
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    @Trace(dispatcher = true)
    public void nrTraceClientSpanKind() {
        System.out.println("Called nrTraceClientSpanKind");
        try {
            clientSpanKind();
            dbClientSpanKind();
            externalClientSpanKind();
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    @Trace(dispatcher = true)
    public void nrTraceProducerSpanKind() {
        System.out.println("Called nrTraceProducerSpanKind");
        try {
            producerSpanKind();
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    // create span with no span kind
    public void noSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("noSpanKind").startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called noSpanKind");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic consumer span
    public void consumerSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("consumerSpanKind").setSpanKind(SpanKind.CONSUMER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called consumerSpanKind");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic producer span
    public void producerSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("producerSpanKind").setSpanKind(SpanKind.PRODUCER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called producerSpanKind");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic server span
    public void serverSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("serverSpanKind").setSpanKind(SpanKind.SERVER)
                .setAttribute("url.path", "/whatever")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called serverSpanKind");
            Thread.sleep(1500);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic client span
    public void clientSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("clientSpanKind").setSpanKind(SpanKind.CLIENT).startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called clientSpanKind");
            Thread.sleep(2000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create DB client span
    public void dbClientSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("owners select").setSpanKind(SpanKind.CLIENT)
                .setAttribute("db.system", "mysql")
                .setAttribute("db.operation", "select")
                .setAttribute("db.sql.table", "owners")
                .setAttribute("db.statement", "SELECT * FROM owners WHERE ssn = 4566661792").startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called dbClientSpanKind");
            Thread.sleep(2000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create external client span
    public void externalClientSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("example.com").setSpanKind(SpanKind.CLIENT)
                .setAttribute("server.address", "www.foo.bar")
                .setAttribute("url.full", "https://www.foo.bar:8080/search?q=OpenTelemetry#SemConv")
                .setAttribute("server.port", 8080)
                .setAttribute("http.request.method", "GET")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("Called externalClientSpanKind");
            Thread.sleep(2000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
