package com.invoice.configuration;

import com.invoice.util.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class Initialization {

    @PostConstruct
    public void init() {

        Constants.COUNTRIES_MAP = new HashMap<>();
        Constants.COUNTRIES_MAP.put("SA","المملكة العربية السعودية");

    }
}
