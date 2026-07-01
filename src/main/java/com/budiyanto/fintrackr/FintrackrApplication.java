package com.budiyanto.fintrackr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(sharedModules = "shared")
public class FintrackrApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintrackrApplication.class, args);
    }

}
