package com.budiyanto.fintrackr.investmentservice.brokeraccount.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Broker {

    private String name;
    private String url;

    public static Broker create(String name, String url) {
        return new Broker(name, url);
    }

}