package com.adoptapp.followupservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FollowupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FollowupServiceApplication.class, args);
    }

}
