package com.alias.openapiservice.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapiservice.mapper.UserInterfaceInfoMapper;
import com.alias.openapiservice.mapper.UserMapper;
import com.alias.openapiservice.service.InterfaceInfoService;
import com.alias.openapiservice.service.UserInterfaceInfoService;
import com.alias.openapiservice.service.UserService;
import com.alias.openapicommon.model.entity.User;
import com.alias.openapiservice.util.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.alias.openapiservice.constant.UserConstant.ADMIN_ROLE;
import static com.alias.openapiservice.constant.UserConstant.USER_LOGIN_STATE;

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
        if (StringUtils.isAnyBlank(account, password, checkPassword)) {
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

            if (Objects.equals(redisTemplate.opsForValue().get(email), code)) {
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
            if (redisTemplate.opsForValue().get(email) != null) {
                // 删除验证码
                redisTemplate.delete(email);
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
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
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
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public boolean sendEmail(String toEmail) {
        if (toEmail == null || StringUtils.isBlank(toEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请填写邮箱地址");
        }

        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code = {}", code);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        //设置发件邮箱
        simpleMailMessage.setFrom(fromEmail);
        //收件人邮箱
        simpleMailMessage.setTo(toEmail);
        //主题标题
        simpleMailMessage.setSubject("ALIAS-API开放平台验证码");
        //信息内容
        simpleMailMessage.setText("您的验证码是：" + code + "\n" + "五分钟内有效");
        //执行发送
        try {//发送可能失败
            javaMailSender.send(simpleMailMessage);
            //将生成的验证码缓存到redis中，设置有效期为5分钟
            redisTemplate.opsForValue().set(toEmail, code, 5, TimeUnit.MINUTES);
            //没有异常返回true，表示发送成功
            return true;
        } catch (Exception e) {
            //发送失败，返回false
            e.printStackTrace();
        }
        return false;
    }
}
