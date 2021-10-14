package com.newrelic.basecontrollerexample.inheritance2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/arbitraryuniquepath")
public class ParentController2 {
    @GetMapping("/method")
    public String getStuff() {
        String stuff = "Stuff";
        stuff = "More " + stuff;
        return stuff;
    }
}
