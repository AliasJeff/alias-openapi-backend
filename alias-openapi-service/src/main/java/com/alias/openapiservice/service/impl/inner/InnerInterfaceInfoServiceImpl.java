package com.alias.openapiservice.service.impl.inner;

import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapiservice.mapper.InterfaceInfoMapper;
import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapicommon.service.InnerInterfaceInfoService;
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

    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        URI uri = URI.create(url);
        String path = uri.getPath();
        log.info("path: {}", path);
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }
}
