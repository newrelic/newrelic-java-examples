package com.nr.vertx.message;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class MessageReceiverVerticle extends AbstractVerticle {
    @Override
    public void start() {
        final EventBus eventBus = vertx.eventBus();
        eventBus.consumer("inbox", receivedMessage -> {
            System.out.println("Received message: " + receivedMessage.body());
            receivedMessage.replyAndRequest("Yo I got your message");
        });
        System.out.println("MessageReceiverVerticle ready");
    }
}
