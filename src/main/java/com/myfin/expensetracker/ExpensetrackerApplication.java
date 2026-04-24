package com.myfin.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.myfin.expensetracker.*")
public class ExpensetrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpensetrackerApplication.class, args);
    }

}
