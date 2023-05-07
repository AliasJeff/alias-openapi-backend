package com.alias.openapiservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.*;
import org.springframework.context.ApplicationContext;

@Slf4j
@MapperScan("com.alias.openapiservice.mapper")
//@ServletComponentScan
@EnableTransactionManagement
@EnableCaching
@SpringBootApplication
@EnableDubbo
//@ComponentScan(basePackages = {"com.alias.openapiservice"})
public class ServiceApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServiceApplication.class, args);
        log.info("Service Application started...");
//        Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
    }

}