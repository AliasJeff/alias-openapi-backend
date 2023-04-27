package com.alias.openapiservice.service;

import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
* @author zhexun
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-04-12 14:14:30
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 初始化接口免费调用次数
     */
    void addUserInterfaceInfo();

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 获取用户可用接口
     * @param interfaceInfoQueryRequest
     * @param userId
     * @return
     */
    IPage<InterfaceInfo> getAvailableInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest, long userId);
}
