package com.alias.openinterface.service.impl;

import com.alias.openapicommon.model.entity.User;
import com.alias.openinterface.mapper.AuthMapper;
import com.alias.openinterface.model.entity.Auth;
import com.alias.openinterface.service.AuthService;
import com.alias.openinterface.util.AuthUtils;
import com.alias.openinterface.util.RequireAllControllerMethodsUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth> implements AuthService {

    @Resource
    private AuthUtils authUtils;

    @Resource
    private RequireAllControllerMethodsUtils utils;

    @Resource
    private ApplicationContext context;

    @Override
    public String mainRedirect(HttpServletRequest request) {
        Map<String, String> headers = authUtils.getHeaders(request);
        //验证请求参数和密钥等是否合法
        boolean isAuth = authUtils.isAuth(headers);
        if (isAuth) {
            String method = request.getMethod();

            //1、获取当前请求路径中的类名和方法
            Map<String, String> hashmap = utils.hashmap;
            String url = headers.get("url");
            String key = "[" + url + "]" ;
            String res = hashmap.get(key);
            log.info("url: {}", url);
            log.info("key: {}", key);
            log.info("res: {}", res);
            if(res == null){
                log.error("AuthService...res is null");
                return null;
            }
            String[] split = res.split("-");
            Object body = null;
            try {
                //通过反射构造
                Class<?> forName = Class.forName(split[0]);
                //由于是object对象，所以实例化对象需要从容器中拿到
                Method classMethod = forName.getMethod(split[1], Object.class);
                log.info("classMethod: {}", classMethod.toString());
                //调用方法
                body = classMethod.invoke(context.getBean(forName), headers.get("body"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(body);
        }
        return null;
    }
}
