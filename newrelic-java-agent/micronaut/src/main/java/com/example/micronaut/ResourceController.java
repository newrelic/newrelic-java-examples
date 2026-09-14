package com.example.micronaut;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Controller
@ExecuteOn(TaskExecutors.IO)
public class ResourceController {

    private final HttpClient httpClient;

    public ResourceController(@Client("http://localhost:8080") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Get("/{parent}/{parentId}/resource-a/{resourceAId}")
    public Map<String, Object> getResourceA(String parent, String parentId, String resourceAId) {
        return Map.of("parent", parent, "parentId", parentId, "resourceAId", resourceAId);
    }

    @Get("/{parent}/{parentId}/resource-b/{resourceBId}")
    public Map<String, Object> getResourceB(String parent, String parentId, String resourceBId) {
        return Map.of("parent", parent, "parentId", parentId, "resourceBId", resourceBId);
    }

    @Put("/{parent}/{parentId}/resource-b/{resourceBId}/action")
    @Consumes(MediaType.ALL)
    public HttpResponse<?> putResourceBAction(String parent, String parentId, String resourceBId, @Body String body) {
        return HttpResponse.noContent();
    }

    @Post("/{parent}/{parentId}/resource-b/{action}")
    @Consumes(MediaType.ALL)
    public HttpResponse<?> postResourceBAction(String parent, String parentId, String action, @Body String body) {
        return HttpResponse.created(
                Map.of("parent", parent, "parentId", parentId, "action", action, "bodyLength", body.length()));
    }

    @Get("/{parent}/{parentId}/resource-a/{resourceAId}/reactive")
    public Mono<Map<String, Object>> getResourceAReactive(String parent, String parentId, String resourceAId) {
        return Mono.fromCallable(() -> Map.<String, Object>of(
                        "parent", parent, "parentId", parentId, "resourceAId", resourceAId, "reactive", true))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Get("/chain")
    public Mono<String> chain() {
        return Mono.from(httpClient.retrieve(HttpRequest.GET("/acme/1/resource-a/42")));
    }
}
