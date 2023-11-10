package com.nr.vertx.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class Receiver extends AbstractVerticle {
    @Override
    public void start() {
        final EventBus eventBus = vertx.eventBus();
        eventBus.consumer("inbox", receivedMessage -> System.out.println("Received message: " + receivedMessage.body()));
        System.out.println("MessageReceiverVerticle ready");
    }
}
