package com.nr.vertx.clustered;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Sender extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final Router router = Router.router(vertx);
        router.post("/send/:message").handler(this::sendMessage);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(config()
                        .getInteger("http.server.port", 8080), result -> {
                    if (result.succeeded()) {
                        System.out.println("HttpServerVerticle running on 8080; use /send/:message to send a message");
                        startPromise.complete();
                    } else {
                        System.out.println("Could not start a HTTP server " +  result.cause());
                        startPromise.fail(result.cause());
                    }
                });
    }

    private void sendMessage(RoutingContext routingContext){
        final EventBus eventBus = vertx.eventBus();
        final String message = routingContext.request().getParam("message");
        eventBus.publish("inbox", message);
        routingContext.response().end("Sent msg: " + message);
        System.out.println("Sent message: " + message);
    }
}
