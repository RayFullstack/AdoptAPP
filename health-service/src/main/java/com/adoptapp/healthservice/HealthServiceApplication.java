package com.adoptapp.healthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class HealthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthServiceApplication.class, args);
    }

}
