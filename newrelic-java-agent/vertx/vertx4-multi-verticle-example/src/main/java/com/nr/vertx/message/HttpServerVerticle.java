package com.nr.vertx.message;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class HttpServerVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.end("Hello from non-clustered vertx app");
        });
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
        eventBus.request("inbox", message, reply -> {
            if (reply.succeeded()) {
                System.out.println("Received reply: " + reply.result().body());
            } else {
                System.out.println("No reply    " + reply.cause());
            }
        });
        routingContext.response().end("Sent msg: " + message);
    }

}
