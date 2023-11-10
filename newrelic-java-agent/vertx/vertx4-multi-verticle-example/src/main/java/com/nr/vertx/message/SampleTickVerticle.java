package com.nr.vertx.message;

import io.vertx.core.AbstractVerticle;

import java.util.concurrent.atomic.AtomicInteger;

public class SampleTickVerticle extends AbstractVerticle {
    AtomicInteger counter = new AtomicInteger(0);

    public void start() {
        vertx.setPeriodic(10000, timerId -> {
           System.out.println("tick - " + counter.incrementAndGet());
        });

        System.out.println("SampleTickVerticle ready");
    }
}
