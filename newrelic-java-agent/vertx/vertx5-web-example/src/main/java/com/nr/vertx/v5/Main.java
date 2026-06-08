package com.nr.vertx.v5;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServerVerticle())
                .onSuccess(id -> System.out.println("vertx5-web-example running on http://localhost:8080"))
                .onFailure(err -> System.err.println("Failed to deploy: " + err.getMessage()));
    }
}
