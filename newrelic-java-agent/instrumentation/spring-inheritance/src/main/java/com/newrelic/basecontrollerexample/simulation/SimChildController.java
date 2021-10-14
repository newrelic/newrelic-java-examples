package com.newrelic.basecontrollerexample.simulation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/childroot")
public class SimChildController extends SimParentController {

    @GetMapping("/childmethod")
    public String getResource() {
        return "Simulate child path";
    }

//    @GetMapping("/parentmethod")
//    public String simulateExpectedParentPath() {
//        return "Simulate expected parent path";
//    }
}
