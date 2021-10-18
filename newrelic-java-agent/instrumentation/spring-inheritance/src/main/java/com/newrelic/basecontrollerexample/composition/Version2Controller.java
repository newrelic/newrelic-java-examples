package com.newrelic.basecontrollerexample.composition;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/composed/v2")
public class Version2Controller {

    private final GetStuffAndDoThingsService service;

    public Version2Controller(GetStuffAndDoThingsService service) {
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
        return "Second version done doing stuff";
    }
}
