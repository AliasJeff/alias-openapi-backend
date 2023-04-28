package com.alias.openapicommon.service;

import com.alias.openapicommon.model.entity.User;

/**
 * 用户服务
* @author zhexun
*/
public interface InnerUserService {

    /**
     * 在数据库查询是否已分配给用户密钥
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

}
