/*
 *  Copyright 2020 New Relic Corporation. All rights reserved.
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.example.kafkaproducer;

import com.newrelic.api.agent.ConcurrentHashMapHeaders;
import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@RestController
@RequestMapping("kafka")
public class Controller {
    private static final String W3C_TRACE_PARENT = "traceparent";
    private static final String W3C_TRACE_STATE = "tracestate";

    @Autowired
    private KafkaTemplate<String, String> producer;

    /**
     * Publishes a Kafka record to a Kafka broker.
     *
     * @return String detailing the published record
     */
    @GetMapping("/produce")
    @Trace(dispatcher = true)
    private String produce() {
        int randomInt = getRandomInt();

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("example-topic", "example-key-" + randomInt, "example-value-" + randomInt);
        producer.send(producerRecord);

        String publishedRecordMessage = String.format("%nPublished Kafka Record:%n\ttopic = %s, key = %s, value = %s%n", producerRecord.topic(),
                producerRecord.key(), producerRecord.value());

        System.out.println(publishedRecordMessage);
        return publishedRecordMessage;
    }

    private int getRandomInt() {
        return new Random().nextInt(1000);
    }

}
