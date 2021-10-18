package com.newrelic.basecontrollerexample.composition;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/composed/v1")
public class Version1Controller {

    private final GetStuffAndDoThingsService service;

    public Version1Controller(GetStuffAndDoThingsService service) {
        this.service = service;
    }

    @GetMapping("/method")
    public String getStuff() {
        String stuff = service.getStuffAndDoThings();
        return stuff;
    }

    @PostMapping
    public String doStuff() {
        // do things
        return "First version done doing stuff";
    }
}
