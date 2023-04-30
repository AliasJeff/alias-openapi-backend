package com.alias.openinterface.controller;

import com.alias.openapicommon.model.entity.User;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * 名称 API
 *
 */
@Slf4j
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(Object name) throws UnsupportedEncodingException {
        log.info("getNameByGet...");
        byte[] bytes = name.toString().getBytes("iso8859-1");
        name = new String(bytes, StandardCharsets.UTF_8);
        return "(GET) 你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "(POST) 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUsernameByPost(Object object) {
        log.info("name...object: {}", object);
        Gson gson = new Gson();
        User user = gson.fromJson(object.toString(), User.class);
        return "(POST) 用户名字是" + user.getUsername();
    }
}
