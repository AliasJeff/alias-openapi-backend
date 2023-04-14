package com.alias.clientsdk;

import com.alias.clientsdk.client.AliasOpenapiClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("alias.openapi.clint")
@ComponentScan
public class ClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public AliasOpenapiClient aliasOpenapiClient() {
        return new AliasOpenapiClient(accessKey, secretKey);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

}
