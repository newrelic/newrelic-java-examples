/*
 *  Copyright 2020 New Relic Corporation. All rights reserved.
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.example.kafkaproducer.controller;

import com.example.kafkaproducer.config.KafkaProducerFactory;
import com.newrelic.api.agent.ConcurrentHashMapHeaders;
import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@RestController
@RequestMapping("kafka")
public class KafkaProducerController {
    private static final String W3C_TRACE_PARENT = "traceparent";
    private static final String W3C_TRACE_STATE = "tracestate";

    /**
     * Publishes a Kafka record to a Kafka broker.
     *
     * @return String detailing the published record
     */
    @GetMapping("/produce")
    private String produce() {

        KafkaTemplate<String, String> producer = KafkaProducerFactory.kafkaTemplate();
        int randomInt = getRandomInt();

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("example-topic", "example-key-" + randomInt, "example-value-" + randomInt);
        addDistributedTraceHeadersToKafkaRecord(producerRecord);
        producer.send(producerRecord);

        String publishedRecordMessage = String.format("%nPublished Kafka Record:%n\ttopic = %s, key = %s, value = %s%n", producerRecord.topic(),
                producerRecord.key(), producerRecord.value());

        System.out.println(publishedRecordMessage);
        return publishedRecordMessage;
    }

    /**
     * This method illustrates usage of New Relic Java agent APIs for propagating distributed tracing headers over Kafka records.
     * NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(Headers) is used to generate distributed tracing headers and insert them into
     * a provided Headers map. This API generates a New Relic header (`newrelic`) as well as W3C Trace Context headers (`traceparent`, `tracestate`).
     *
     * @param producerRecord Kafka record
     */
    @Trace
    private void addDistributedTraceHeadersToKafkaRecord(ProducerRecord<String, String> producerRecord) {
        // ConcurrentHashMapHeaders provides a concrete implementation of com.newrelic.api.agent.Headers
        Headers distributedTraceHeaders = ConcurrentHashMapHeaders.build(HeaderType.MESSAGE);
        // Generate W3C Trace Context headers and insert them into the distributedTraceHeaders map
        NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(distributedTraceHeaders);

        // Retrieve the generated W3C Trace Context headers and insert them into the ProducerRecord headers
        if (distributedTraceHeaders.containsHeader(W3C_TRACE_PARENT)) {
            producerRecord.headers().add(W3C_TRACE_PARENT, distributedTraceHeaders.getHeader(W3C_TRACE_PARENT).getBytes(StandardCharsets.UTF_8));
        }

        if (distributedTraceHeaders.containsHeader(W3C_TRACE_STATE)) {
            producerRecord.headers().add(W3C_TRACE_STATE, distributedTraceHeaders.getHeader(W3C_TRACE_STATE).getBytes(StandardCharsets.UTF_8));
        }
    }

    private int getRandomInt() {
        return new Random().nextInt(1000);
    }

}
