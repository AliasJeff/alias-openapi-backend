package com.alias.openapiservice.service;

import com.alias.openapicommon.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public interface UserService extends IService<User> {

    /**
     * 发送邮箱验证码
     * @param toEmail
     * @return
     */
    boolean sendEmail(String toEmail);

    /**
     * 用户注册
     *
     * @param account
     * @param password
     * @param checkPassword
     * @return 新用户id
     */
    Long register(String account, String password, String checkPassword, String email, String code);

    /**
     * 用户登录
     *
     * @param account
     * @param password
     * @return 脱敏后的信息
     */
    User login(HttpServletRequest request, String account, String password);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean logout(HttpServletRequest request);

    /**
     * 获取github+gitee star数
     * @return
     */
    Integer getStars() throws IOException;
}
