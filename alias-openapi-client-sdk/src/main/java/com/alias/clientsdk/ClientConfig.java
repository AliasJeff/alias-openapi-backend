package com.alias.clientsdk;

import com.alias.clientsdk.client.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;

@Data
@Configuration
@ConfigurationProperties("alias.openapi.client")
@ComponentScan
public class ClientConfig {

    private Integer appId;

    private String accessKey;

    private String secretKey;

    @Bean
    public ApiClient apiClient() {
        ApiClient client = null;
        try {
            Class<?> forName = Class.forName("com.alias.clientsdk.client.ApiClient");
            Constructor<?> declaredConstructor = forName.getDeclaredConstructor(Integer.class, String.class, String.class);
            declaredConstructor.setAccessible(true);
            client = (ApiClient) declaredConstructor.newInstance(appId, accessKey, secretKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return client;
    }

}
