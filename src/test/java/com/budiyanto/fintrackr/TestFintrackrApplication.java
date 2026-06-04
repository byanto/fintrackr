package com.budiyanto.fintrackr;

import org.springframework.boot.SpringApplication;

public class TestFintrackrApplication {

    public static void main(String[] args) {
        SpringApplication.from(FintrackrApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
