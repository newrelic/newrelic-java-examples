package com.example.micronaut;

import com.newrelic.api.agent.NewRelic;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;

@ServerFilter(Filter.MATCH_ALL_PATTERN)
public class CustomAttributeFilter {

    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        NewRelic.setTransactionName(null, request.getMethodName() + " - " + request.getPath());
        NewRelic.addCustomParameter("custom.route", request.getPath());
        NewRelic.addCustomParameter("custom.role", "unknown");
        NewRelic.addCustomParameter("custom.flag", "false");
    }

    @ResponseFilter
    public void onResponse(HttpRequest<?> request, @Nullable HttpResponse<?> response) {
        NewRelic.addCustomParameter("custom.role", "unknown");
        NewRelic.addCustomParameter("custom.flag", response != null ? String.valueOf(response.getStatus().getCode()) : "unknown");
    }
}
