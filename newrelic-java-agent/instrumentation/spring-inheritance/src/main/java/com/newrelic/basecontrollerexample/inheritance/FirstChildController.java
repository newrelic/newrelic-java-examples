package com.newrelic.basecontrollerexample.inheritance;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inherited/v1")
public class FirstChildController extends ParentController {
    @PostMapping
    public String doStuff() {
        // do things
        return "First child done doing stuff";
    }
}
