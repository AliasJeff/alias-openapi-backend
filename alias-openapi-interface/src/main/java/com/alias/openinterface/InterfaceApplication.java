package com.alias.openinterface;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@MapperScan("com.alias.openinterface.mapper")
public class InterfaceApplication {

    public static void main(String[] args) {
        System.out.println("Interface Application started...");
        SpringApplication.run(InterfaceApplication.class, args);
    }

}
