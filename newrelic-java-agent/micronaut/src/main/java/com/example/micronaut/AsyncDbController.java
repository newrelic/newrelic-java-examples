package com.example.micronaut;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Controller("/async-db")
public class AsyncDbController {

    @Get("/ping/{id}")
    public HttpResponse<String> ping(String id) {
        return HttpResponse.ok("pong " + id);
    }

    @Get("/{id}")
    public HttpResponse<String> launchDelayed(String id) {
        Mono.fromCallable(() -> simulateSlowDbCall(id))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return HttpResponse.ok("launched " + id);
    }

    @Get("/{id}/leak")
    public HttpResponse<String> launchNeverCompletes(String id) {
        Mono.never()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return HttpResponse.ok("launched (never completes) " + id);
    }

    private String simulateSlowDbCall(String id) {
        try {
            Thread.sleep(Duration.ofSeconds(5).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "result-for-" + id;
    }
}
