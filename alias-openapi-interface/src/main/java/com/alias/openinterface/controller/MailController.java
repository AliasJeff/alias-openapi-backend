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

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromEmail);
        simpleMailMessage.setTo(mail.getToMail());
        simpleMailMessage.setSubject("ALIAS-API开放平台用户邮件");
        simpleMailMessage.setText(mail.getContent());

        try {
            javaMailSender.send(simpleMailMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "发送邮件成功";
    }
}
