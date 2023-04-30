package com.alias.openapiservice.service;

import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapiservice.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author zhexun
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-04-08 23:12:03
*/
@Service
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 获取所有接口的总调用次数
     * @return
     */
    List<InterfaceInfoVO> getInterfaceInfoTotalInvokeCount();
}
