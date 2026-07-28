package com.alness.lifemaster.common.currency;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/catalogs/currencies")
public class CurrencyController {

    @GetMapping
    public List<CurrencyOption> findAll() {
        return Arrays.stream(CurrencyCode.values())
                .map(currency -> new CurrencyOption(currency.name(), currency.getName()))
                .toList();
    }
}
