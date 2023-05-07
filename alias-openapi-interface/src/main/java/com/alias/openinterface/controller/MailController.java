package com.alias.openinterface.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONParser;
import cn.hutool.json.ObjectMapper;
import com.alias.openinterface.model.entity.Mail;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @PostMapping("/sendMail")
    public String sendMail(Object object) {

        log.info("sendMail...object: {}", object.toString());
        Gson gson = new Gson();
        Mail mail = gson.fromJson(object.toString(), Mail.class);
        log.info("sendMail...mail: {}", mail);

        if (StringUtils.isAnyBlank(mail.getToMail(), mail.getContent())) {
            return "请填写收件人和内容";
        }

        // 发件人电子邮箱
        String from = "zhexunchen@qq.com";

        // 指定发送邮件的主机为 smtp.qq.com
        String host = "smtp.qq.com";

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.auth", "true");

        //阿里云服务器禁用25端口，所以服务器上改为465端口
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", "465");

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("zhexunchen@qq.com", "ualqybfjbbnhcaab"); //发件人邮件用户名、密码
            }
        });

        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(mail.getToMail()));

            // Set Subject: 头部头字段
            message.setSubject("ALIAS-API开放平台用户邮件");

            // 设置消息体
            message.setText(mail.getContent());

            // 发送消息
            Transport.send(message);
            return "发送邮件成功！";
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return "发送邮件失败，请检查";
        }
    }
}
