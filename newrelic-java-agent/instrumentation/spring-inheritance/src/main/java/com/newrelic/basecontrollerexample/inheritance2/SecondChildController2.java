package com.newrelic.basecontrollerexample.inheritance2;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inherited2/v2")
public class SecondChildController2 extends ParentController2 {

    @PostMapping
    public String doStuff() {
        // do things
        return "Second child done doing stuff";
    }
}
