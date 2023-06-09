package com.alias.openapiservice.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapiservice.mapper.UserMapper;
import com.alias.openapiservice.service.UserService;
import com.alias.openapicommon.model.entity.User;
import com.alias.openapiservice.util.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.alias.openapiservice.constant.UserConstant.ADMIN_ROLE;
import static com.alias.openapiservice.constant.UserConstant.USER_LOGIN_STATE;
import static com.alias.openapiservice.constant.RedisConstant.*;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final Lock lock = new ReentrantLock();

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "alias";

    @Override
    public Long register(String account, String password, String checkPassword, String email, String code) {
        // 1. 校验
        if (StringUtils.isAnyBlank(account, password, checkPassword, email, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (account.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过长");
        }
        if (password.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (password.length() > 20 || checkPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过长");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        // 3. 分配accessKey、secretKey
        String accessKey = DigestUtil.md5Hex(SALT + account + RandomUtil.randomNumbers(4));
        String secretKey = DigestUtil.md5Hex(SALT + account + RandomUtil.randomNumbers(8));

        // 4. 插入数据
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        user.setEmail(email);
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);

        // 加锁
        lock.lock();
        try {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("account", account);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }

            if (Objects.equals(redisTemplate.opsForValue().get(REGISTER_EMAIL_PREFIX + email), code)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
            }

            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }

            return user.getId();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (redisTemplate.opsForValue().get(REGISTER_EMAIL_PREFIX + email) != null) {
                // 删除验证码
                redisTemplate.delete(REGISTER_EMAIL_PREFIX + email);
            }
            // 释放锁
            lock.unlock();
        }
        return null;
    }


    @Override
    public User login(HttpServletRequest request, String account, String password) {
        // 1. 校验
        if (StringUtils.isAnyBlank(account, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        queryWrapper.eq("password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, account cannot match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 脱敏
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setAccount(user.getAccount());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setAvatar(user.getAvatar());
        safetyUser.setGender(user.getGender());
        safetyUser.setRole(user.getRole());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUpdateTime(user.getUpdateTime());

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();

        // 从数据库查询
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        request.getSession().setAttribute(USER_LOGIN_STATE, currentUser);
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean logout(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        redisTemplate.delete(LOGIN_USER_PREFIX + user.getId() + "_" + USER_LOGIN_STATE);
        return true;
    }

    @Override
    public Integer getStars() throws IOException {
        // 从缓存查询
        Integer redisStars = (Integer) redisTemplate.opsForValue().get(GITHUB_STARS_PREFIX);
        if (redisStars != null) {
            return redisStars;
        }

        // 获取github stars
        String listContent = null;
        try {
            listContent= HttpUtil.get("https://img.shields.io/github/stars/AliasJeff?style=social");
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取GitHub Starts 超时");
        }
        //该操作查询时间较长
        List<String> titles = ReUtil.findAll("<title>(.*?)</title>", listContent, 1);
        String str = null;
        for (String title : titles) {
            //打印标题
            String[] split = title.split(":");
            str = split[1];
        }

        Integer githubStars = Integer.parseInt(str.trim());

        // 获取gitee star数（可能超出请求限制）
        String owner = "AliasJeff";
        String repo1 = "alias-openapi-frontend";
        String repo2 = "alias-openapi-backend";
        String url1 = "https://gitee.com/api/v5/repos/" + owner + "/" + repo1;
        String url2 = "https://gitee.com/api/v5/repos/" + owner + "/" + repo2;

        OkHttpClient client = new OkHttpClient();

        Integer giteeStars = 0;
        try {
            Integer starCount1 = getStarCount(client, url1);
            Integer starCount2 = getStarCount(client, url2);
            giteeStars = starCount1 + starCount2;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 加入缓存
        redisTemplate.opsForValue().set(GITHUB_STARS_PREFIX, giteeStars + githubStars, 1, TimeUnit.MINUTES);

        return githubStars + giteeStars;
    }

    private static Integer getStarCount(OkHttpClient client, String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        String responseData = response.body().string();
        System.out.println(responseData);
        JSONObject jsonObject = new JSONObject(responseData);
        return jsonObject.getInt("stargazers_count");
    }

    @Override
    public boolean sendEmail(String toEmail) {
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
                    new InternetAddress(toEmail));

            // Set Subject: 头部头字段
            message.setSubject("ALIAS-API开放平台验证码");

            // 设置消息体
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code: {}", code);
            redisTemplate.opsForValue().set(REGISTER_EMAIL_PREFIX + toEmail, code, 5, TimeUnit.MINUTES);
            message.setText("您的验证码是：" + code + "\n" + "五分钟内有效");

            // 发送消息
            Transport.send(message);
            return true;
        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
        }
    }
}
