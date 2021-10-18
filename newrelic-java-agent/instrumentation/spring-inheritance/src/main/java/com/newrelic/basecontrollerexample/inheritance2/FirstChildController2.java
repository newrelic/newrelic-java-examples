package com.newrelic.basecontrollerexample.inheritance2;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inherited2/v1")
public class FirstChildController2 extends ParentController2 {
    @PostMapping
    public String doStuff() {
        // do things
        return "First child done doing stuff";
    }
}
