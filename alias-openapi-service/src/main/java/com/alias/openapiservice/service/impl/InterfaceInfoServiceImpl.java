package com.alias.openapiservice.service.impl;

import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapiservice.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alias.openapiservice.mapper.InterfaceInfoMapper;
import com.alias.openapiservice.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author zhexun
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-04-08 23:12:03
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

    @Override
    public List<InterfaceInfoVO> getInterfaceInfoTotalInvokeCount() {
        return interfaceInfoMapper.getInterfaceTotalInvokeCounts();
    }
}




