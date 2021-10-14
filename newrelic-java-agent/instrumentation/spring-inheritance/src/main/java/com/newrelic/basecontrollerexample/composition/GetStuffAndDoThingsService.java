package com.newrelic.basecontrollerexample.composition;

import org.springframework.stereotype.Service;

@Service
public class GetStuffAndDoThingsService {
    public String getStuffAndDoThings() {
        String stuff = "Stuff";
        stuff = "More " + stuff;
        return stuff;
    }
}
