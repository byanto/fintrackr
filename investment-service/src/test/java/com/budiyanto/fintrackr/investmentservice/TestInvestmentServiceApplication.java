package com.budiyanto.fintrackr.investmentservice;

import org.springframework.boot.SpringApplication;

public class TestInvestmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(InvestmentServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
