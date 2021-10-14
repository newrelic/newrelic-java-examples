package com.newrelic.basecontrollerexample.simulation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/parent")
public class SimParentController {
    @GetMapping("/parentmethod")
    public String parentMethod() {
        return "Sim Parent Path";
    }
}
