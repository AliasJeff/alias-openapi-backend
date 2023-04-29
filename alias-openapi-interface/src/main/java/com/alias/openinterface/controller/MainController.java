package com.alias.openinterface.controller;

import cn.hutool.http.HttpRequest;
import com.alias.openapicommon.service.InnerUserService;
import com.alias.openinterface.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class MainController {

    @Resource
    private AuthService authService;

    /**
     * 请求转发
     * @param request
     */
    @RequestMapping("/main")
    public String MainRedirect(HttpServletRequest request) {
        log.info("mainRedirect....");
        return authService.mainRedirect(request);
    }

}
