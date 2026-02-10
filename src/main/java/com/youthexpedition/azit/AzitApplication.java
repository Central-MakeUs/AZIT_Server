package com.youthexpedition.azit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableFeignClients
@EnableRetry
@SpringBootApplication
public class AzitApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzitApplication.class, args);
    }

}
