package com.alias.openapigateway;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@SpringBootApplication
@EnableDubbo
@Service
public class GatewayApplication {

    public static void main(String[] args) {
        System.out.println("Gateway Application started...");
        SpringApplication.run(GatewayApplication.class, args);
    }

}
