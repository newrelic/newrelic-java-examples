package com.newrelic.basecontrollerexample.inheritance;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inherited/v2")
public class SecondChildController extends ParentController {

    @PostMapping
    public String doStuff() {
        // do things
        return "Second child done doing stuff";
    }
}
