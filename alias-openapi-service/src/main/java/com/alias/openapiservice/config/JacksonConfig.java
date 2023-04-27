package com.alias.openapiservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.annotation.Resource;


/**
 * 统一注解，解决前后端交互Long类型精度丢失的问题
 */
@Slf4j
@Configuration
public class JacksonConfig {

    @Resource
    private Jackson2ObjectMapperBuilder builder;

    @Bean
    public ObjectMapper jacksonObjectMapper() {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //全局配置序列化返回json处理
        SimpleModule simpleModule = new SimpleModule();
        //json Long ==>String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}