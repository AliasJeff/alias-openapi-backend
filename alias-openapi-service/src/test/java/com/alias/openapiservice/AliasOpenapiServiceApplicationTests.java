package com.alias.openapiservice;

import com.alias.openapiservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class AliasOpenapiServiceApplicationTests {

    @Resource
    private UserService userService;

}
