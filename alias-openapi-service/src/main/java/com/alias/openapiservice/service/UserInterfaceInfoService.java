package com.alias.openapiservice.service;

import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
