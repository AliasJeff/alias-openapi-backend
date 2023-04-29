package com.alias.openinterface.service;

import com.alias.openapicommon.model.entity.SoulSoup;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author zhexun
* @description 针对表【soul_soup(心灵鸡汤表)】的数据库操作Service
* @createDate 2023-04-29 19:24:10
*/
public interface SoulSoupService extends IService<SoulSoup> {

    /**
     * 随机查找一条心灵鸡汤
     * @return
     */
    String getRandom();

}
