package com.alias.openapicommon.service;

import com.alias.openapicommon.model.entity.InterfaceInfo;

/**
* @author zhexun
*/
public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询接口是否存在（请求路径、请求方法）
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
}
