package com.alias.openapiservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 处理跨域请求
 */
@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级
public class SimpleCORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // 强制转换为HttpServletRequest和HttpServletResponse，方便后期操作
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 添加响应头来允许跨域请求
        // 允许哪些域名访问，这里使用request.getHeader("origin")获取请求的来源域名
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
        // 允许哪些HTTP方法
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        // 预检请求的有效时间
        response.addHeader("Access-Control-Max-Age", "3600");

        response.addHeader("Access-Control-Allow-Credentials", "true");
//        // 判断请求的来源域名和端口是否与当前页面完全一致
//        if (request.getHeader("origin").equals(request.getHeader("host"))) {
//            // 如果一致，则允许发送Cookie
//            response.addHeader("Access-Control-Allow-Credentials", "true");
//        } else {
//            // 如果不一致，则不允许发送Cookie（避免安全风险）
//            response.addHeader("Access-Control-Allow-Credentials", "false");
//        }
        // 允许哪些请求头
        response.addHeader("Access-Control-Allow-Headers", "Content-Type,X-CAF-Authorization-Token,sessionToken,X-TOKEN,customercoderoute,authorization,conntectionid,Cookie");

        // 判断如果是OPTIONS请求，则返回NO_CONTENT状态码，
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            log.info("set response NO_CONTENT...");
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else { // 否则继续执行后续的过滤器或者请求处理器，这里是处理预检请求的逻辑
            log.info("chain.doFilter...");
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

}