package com.alias.openapiservice.controller;

import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapiservice.annotation.AuthCheck;
import com.alias.openapiservice.common.*;
import com.alias.openapiservice.constant.CommonConstant;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapiservice.mapper.UserInterfaceInfoMapper;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.alias.openapiservice.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.alias.openapiservice.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.alias.openapiservice.model.dto.userInterfaceInfo.UserInterfaceInfoUpdateRequest;
import com.alias.openapiservice.service.InterfaceInfoService;
import com.alias.openapiservice.service.UserInterfaceInfoService;
import com.alias.openapiservice.service.UserService;
import com.alias.openapicommon.model.entity.User;
import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.alias.openapiservice.constant.RedisConstant.USER_INTERFACE_PREFIX;

@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
@CrossOrigin
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
        // 校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        Set keys = redisTemplate.keys(USER_INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userInterfaceInfoService.removeById(id);
        Set keys = redisTemplate.keys(USER_INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                         HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        Set keys = redisTemplate.keys(USER_INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = (UserInterfaceInfo) redisTemplate.opsForValue().get(USER_INTERFACE_PREFIX + id);
        if (userInterfaceInfo != null) {
            return ResultUtils.success(userInterfaceInfo);
        }
        userInterfaceInfo = userInterfaceInfoService.getById(id);
        redisTemplate.opsForValue().set(USER_INTERFACE_PREFIX + id, userInterfaceInfo, 60, TimeUnit.MINUTES);
        return ResultUtils.success(userInterfaceInfo);
    }

    @GetMapping("/available")
    public BaseResponse<IPage<InterfaceInfo>> getAvailableInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getId() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        IPage<InterfaceInfo> interfaceInfoIPage = (IPage<InterfaceInfo>) redisTemplate.opsForValue().get(USER_INTERFACE_PREFIX + interfaceInfoQueryRequest.toString());
        if (interfaceInfoIPage != null) {
            return ResultUtils.success(interfaceInfoIPage);
        }

        long userId = loginUser.getId();
        interfaceInfoIPage = userInterfaceInfoService.getAvailableInterfaceInfo(interfaceInfoQueryRequest, userId);
        redisTemplate.opsForValue().set(USER_INTERFACE_PREFIX + interfaceInfoIPage.toString(), interfaceInfoIPage, 60, TimeUnit.MINUTES);
        return ResultUtils.success(interfaceInfoIPage);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        List<UserInterfaceInfo> userInterfaceInfoList = (List<UserInterfaceInfo>) redisTemplate.opsForValue().get(USER_INTERFACE_PREFIX + userInterfaceInfoQuery.toString());
        if (userInterfaceInfoList != null) {
            return ResultUtils.success(userInterfaceInfoList);
        }

        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);
        redisTemplate.opsForValue().set(USER_INTERFACE_PREFIX + userInterfaceInfoQuery, userInterfaceInfoList, 60, TimeUnit.MINUTES);
        return ResultUtils.success(userInterfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        if (userInterfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();

        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Page<UserInterfaceInfo> userInterfaceInfoPage = (Page<UserInterfaceInfo>) redisTemplate.opsForValue().get(USER_INTERFACE_PREFIX + userInterfaceInfoQuery);
        if (userInterfaceInfoPage != null) {
            return ResultUtils.success(userInterfaceInfoPage);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size), queryWrapper);
        redisTemplate.opsForValue().set(USER_INTERFACE_PREFIX + userInterfaceInfoQuery, userInterfaceInfoPage, 60, TimeUnit.MINUTES);
        return ResultUtils.success(userInterfaceInfoPage);
    }

    @GetMapping("/getInvokeCount")
    public BaseResponse<Integer> getInvokeCount(HttpServletRequest request) {
        // 创建Wrapper对象
        QueryWrapper<UserInterfaceInfo> wrapper = new QueryWrapper<>();
        // 查询条件
        wrapper.eq("is_delete", 0);
        // 聚合函数sum()
        wrapper.select("sum(total_num) as totalNum");
        // 执行查询
        Map<String, Object> resultMap = userInterfaceInfoMapper.selectMaps(wrapper).get(0);
        // 获取总调用次数的总和
        return ResultUtils.success(Integer.parseInt(resultMap.get("totalNum").toString()));
    }

    @GetMapping("/getStars")
    public BaseResponse<Integer> getStars(HttpServletRequest request) throws IOException {
        return ResultUtils.success(userService.getStars());
    }

}
