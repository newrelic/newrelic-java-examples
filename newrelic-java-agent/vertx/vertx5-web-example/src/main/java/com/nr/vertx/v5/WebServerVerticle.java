package com.nr.vertx.v5;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;

// A single verticle that exercises New Relic instrumentation hooks added for Vert.x 5.
public class WebServerVerticle extends AbstractVerticle {

    private static final int PORT = 8080;

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        router.get("/api/hello").handler(ctx ->
                ctx.response().end("Hello from Vert.x 5!")
        );

        router.get("/api/greet/:name").handler(ctx -> {
            String name = ctx.pathParam("name");
            ctx.response().end("Hello, " + name + "!");
        });

        router.get("/api/chain")
                .handler(ctx -> {
                    ctx.response().putHeader("X-Step", "one");
                    ctx.next();
                })
                .handler(ctx ->
                        ctx.response().end("Chain complete: two handlers executed")
                );

        router.get("/api/blocking/:task").blockingHandler(ctx -> {
            String task = ctx.pathParam("task");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ctx.response().end("Blocking task '" + task + "' completed");
        });

        router.get("/api/compute").handler(ctx ->
                vertx.executeBlocking(() -> {
                    long sum = 0;
                    for (int i = 0; i < 1_000_000; i++) {
                        sum += i;
                    }
                    return sum;
                }).onSuccess(result ->
                        ctx.response().end("Computed sum: " + result)
                ).onFailure(err ->
                        ctx.fail(500, err)
                )
        );

        router.get("/api/fetch").handler(ctx ->
                vertx.createHttpClient()
                        .request(HttpMethod.GET, PORT, "localhost", "/api/hello")
                        .compose(req -> req.send())
                        .compose(resp -> resp.body())
                        .onSuccess(body ->
                                ctx.response().end("Fetched from /api/hello: " + body)
                        )
                        .onFailure(err ->
                                ctx.fail(500, err)
                        )
        );

        Router subRouter = Router.router(vertx);
        subRouter.get("/ping").handler(ctx ->
                ctx.response().end("pong")
        );
        router.route("/sub/*").subRouter(subRouter);

        router.get("/api/fail").handler(ctx ->
                ctx.fail(500, new RuntimeException("Intentional failure for demo"))
        );
        router.errorHandler(500, ctx -> {
            Throwable cause = ctx.failure();
            ctx.response()
                    .setStatusCode(500)
                    .end("Error: " + (cause != null ? cause.getMessage() : "unknown"));
        });

        router.get("/api/fetch-bad").handler(ctx ->
                vertx.createHttpClient()
                        .request(HttpMethod.GET, 80, "host.invalid", "/")
                        .compose(req -> req.send())
                        .onSuccess(resp ->
                                ctx.response().end("Unexpected success")
                        )
                        .onFailure(err ->
                                ctx.response().end("Expected failure: " + err.getClass().getSimpleName()
                                        + " — " + err.getMessage())
                        )
        );

        router.get("/api/reroute").handler(ctx ->
                ctx.reroute("/api/hello")
        );

        router.get("/api/echo-headers").handler(ctx -> {
            StringBuilder sb = new StringBuilder();
            ctx.request().headers().forEach(e ->
                    sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n")
            );
            ctx.response().end(sb.toString());
        });

        router.get("/api/fetch-headers").handler(ctx ->
                vertx.createHttpClient()
                        .request(HttpMethod.GET, PORT, "localhost", "/api/echo-headers")
                        .compose(req -> req.send())
                        .compose(resp -> resp.body())
                        .onSuccess(body ->
                                ctx.response().end("Headers seen by /api/echo-headers:\n" + body)
                        )
                        .onFailure(err ->
                                ctx.fail(500, err)
                        )
        );

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(PORT)
                .onSuccess(s -> {
                    System.out.println("HTTP server started on port " + PORT);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }
}
