package com.nr.vertx.message;

import io.vertx.core.Vertx;

public class VerticleMain {
    public static void main(String [] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MessageReceiverVerticle());
        vertx.deployVerticle(new SampleTickVerticle());
        vertx.deployVerticle(new HttpServerVerticle());
    }
}
