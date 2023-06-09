package com.alias.openapiservice.controller;

import com.alias.clientsdk.client.ApiClient;
import com.alias.clientsdk.model.Api;
import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.alias.openapiservice.annotation.AuthCheck;
import com.alias.openapiservice.annotation.UserInterfaceInfoChanged;
import com.alias.openapiservice.common.*;
import com.alias.openapiservice.constant.CommonConstant;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoUpdateRequest;
import com.alias.openapiservice.model.vo.InterfaceInfoVO;
import com.alias.openapiservice.service.InterfaceInfoService;
import com.alias.openapiservice.service.UserInterfaceInfoService;
import com.alias.openapiservice.service.UserService;
import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapicommon.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.alias.openapiservice.constant.RedisConstant.INTERFACE_PREFIX;
import static com.alias.openapiservice.constant.RedisConstant.USER_INTERFACE_PREFIX;

@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
@CrossOrigin
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    @UserInterfaceInfoChanged
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setCreator(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        Set keys = redisTemplate.keys(INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(newInterfaceInfoId);
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
    @UserInterfaceInfoChanged
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getCreator().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        Set keys = redisTemplate.keys(INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    @UserInterfaceInfoChanged
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getCreator().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        Set keys = redisTemplate.keys(INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(result);
    }


    /**
     * 上线接口
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断接口是否存在
        long id = idRequest.getId();
        log.info("id: {}", id);
        InterfaceInfo oldInterfaceInfo = (InterfaceInfo) redisTemplate.opsForValue().get(INTERFACE_PREFIX + id);
        if (oldInterfaceInfo == null) {
            oldInterfaceInfo = interfaceInfoService.getById(id);
        }
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // todo 验证接口是否可以调用，修改调用
//        User user = new User();
//        user.setUsername("test");
//        String username = aliasOpenapiClient.getUsernameByPost(user);
//        if (StringUtils.isBlank(username)) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
//        }

        // 更新状态
        InterfaceInfo newInterfaceInfo = new InterfaceInfo();
        newInterfaceInfo.setId(id);
        newInterfaceInfo.setStatus(1);
        boolean result = interfaceInfoService.updateById(newInterfaceInfo);
        Set keys = redisTemplate.keys(INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                      HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断接口是否存在
        long id = idRequest.getId();
        log.info("id: {}", id);
        InterfaceInfo oldInterfaceInfo = (InterfaceInfo) redisTemplate.opsForValue().get(INTERFACE_PREFIX + id);
        if (oldInterfaceInfo == null) {
            oldInterfaceInfo = interfaceInfoService.getById(id);
        }
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 更新接口状态
        InterfaceInfo newInterfaceInfo = new InterfaceInfo();
        newInterfaceInfo.setId(id);
        newInterfaceInfo.setStatus(0);
        boolean result = interfaceInfoService.updateById(newInterfaceInfo);
        Set keys = redisTemplate.keys(INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys);
        return ResultUtils.success(result);
    }

    /**
     * 接口调用
     *
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterface(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = interfaceInfoInvokeRequest.getId();
        String userRequestParams = ""; // 防止trim报npe
        if (interfaceInfoInvokeRequest.getUserRequestParams() != null) {
            userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams().trim();
        }
        String method = interfaceInfoInvokeRequest.getMethod();
        String url = interfaceInfoInvokeRequest.getUrl();
        log.info("invoke...userRequestParams: {}", userRequestParams);
        log.info("invoke...method: {}", method);
        log.info("invoke...url: {}", url);

        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 判断接口是否已关闭
        if (oldInterfaceInfo.getStatus() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口已关闭");
        }

        // 判断请求方法、请求地址是否为空
        if (StringUtils.isAnyBlank(method, url)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 接口调用
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        Api api = new Api();
        api.setInterfaceId(String.valueOf(id));
        api.setId(loginUser.getId());
        api.setAccount(loginUser.getAccount());
        api.setBody(userRequestParams);
        api.setUrl(url);
        api.setMethod(method);

        Integer appId = 123; // todo appid
        ApiClient apiClient = new ApiClient(appId, accessKey, secretKey);
        String result = apiClient.getResult(api);

        Set keys1 = (Set) redisTemplate.opsForValue().get(USER_INTERFACE_PREFIX + "*");
        Set keys2 = (Set) redisTemplate.opsForValue().get(INTERFACE_PREFIX + "*");
        redisTemplate.delete(keys1);
        redisTemplate.delete(keys2);

        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);

        if (interfaceInfo == null || interfaceInfo.getId() == null || interfaceInfo.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        User user = userService.getLoginUser(request);
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("interface_info_id", interfaceInfo.getId());
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
        interfaceInfo.setLeftNum(userInterfaceInfo.getLeftNum());

        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }

        List<InterfaceInfo> interfaceInfoList = (List<InterfaceInfo>) redisTemplate.opsForValue().get(INTERFACE_PREFIX + interfaceInfoQuery.toString());
        if (interfaceInfoList != null) {
            return ResultUtils.success(interfaceInfoList);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String content = interfaceInfoQuery.getDescription();
        // content 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Page<InterfaceInfo> interfaceInfoPage = (Page<InterfaceInfo>) redisTemplate.opsForValue().get(INTERFACE_PREFIX + interfaceInfoQuery.toString());
        if (interfaceInfoPage != null) {
            return ResultUtils.success(interfaceInfoPage);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        redisTemplate.opsForValue().set(INTERFACE_PREFIX + interfaceInfoQuery.toString(), interfaceInfoPage, 60, TimeUnit.MINUTES);
        return ResultUtils.success(interfaceInfoPage);
    }

    @GetMapping("/getInterfaceCount")
    public BaseResponse<Integer> getInterfaceCount(HttpServletRequest request) {
        Integer count = Math.toIntExact(interfaceInfoService.count());
        return ResultUtils.success(count);
    }

    @GetMapping("/getInterfaceInvokeCount")
    public BaseResponse<List<InterfaceInfoVO>> getInterfaceInvokeCount(HttpServletRequest request) {
        List<InterfaceInfoVO> list = interfaceInfoService.getInterfaceInfoTotalInvokeCount();
        return ResultUtils.success(list);
    }
}
