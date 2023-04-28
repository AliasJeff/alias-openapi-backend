package com.alias.openapiservice.service.impl.inner;

import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapiservice.mapper.InterfaceInfoMapper;
import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapicommon.service.InnerInterfaceInfoService;
import com.alias.openapiservice.service.UserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.net.URI;

@Slf4j
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        log.info("url: {}", url);
        URI uri = URI.create(url);
        String path = uri.getPath();
        log.info("path: {}", path);
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean hasCount(Long interfaceId, Long userId) {
        if (interfaceId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(new QueryWrapper<UserInterfaceInfo>()
                .eq("interface_info_id", interfaceId)
                .eq("user_id", userId)
                .gt("left_num", 0));

        return userInterfaceInfo != null;
    }
}
