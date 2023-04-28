package com.alias.openinterface.util;

import com.alias.openapicommon.service.InnerInterfaceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YukeSeko
 */
@Component
public class AuthUtils {

    /**
     * 验证用户的调用信息是否正确
     *
     * @param headers
     * @return
     */
    public boolean isAuth(Map<String, String> headers) {
        // todo isAuth
        return true;
    }

    /**
     * 获取请求头中的信息
     *
     * @param request
     * @return
     */
    public Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", request.getHeader("userId"));
        map.put("userAccount", request.getHeader("userAccount"));
        map.put("appId", request.getHeader("appId"));
        map.put("accessKey", request.getHeader("accessKey"));
        map.put("secretKey", request.getHeader("secretKey"));
        map.put("body", request.getHeader("body"));
        map.put("timestamp", request.getHeader("timestamp"));
        map.put("interfaceId", request.getHeader("interfaceId"));
        map.put("url", request.getHeader("url"));
        return map;
    }
}
