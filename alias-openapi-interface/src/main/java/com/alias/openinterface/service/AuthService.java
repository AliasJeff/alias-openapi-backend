package com.alias.openinterface.service;

import com.alias.openinterface.model.entity.Auth;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface AuthService extends IService<Auth> {

    String mainRedirect(HttpServletRequest request);
}
