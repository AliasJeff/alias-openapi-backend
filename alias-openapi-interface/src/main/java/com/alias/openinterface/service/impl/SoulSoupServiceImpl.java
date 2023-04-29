package com.alias.openinterface.service.impl;

import com.alias.openapicommon.model.entity.SoulSoup;
import com.alias.openinterface.mapper.SoulSoupMapper;
import com.alias.openinterface.service.SoulSoupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
* @author zhexun
* @description 针对表【soul_soup(心灵鸡汤表)】的数据库操作Service实现
* @createDate 2023-04-29 19:24:10
*/
@Service
public class SoulSoupServiceImpl extends ServiceImpl<SoulSoupMapper, SoulSoup>
    implements SoulSoupService {

    @Override
    public String getRandom() {
        QueryWrapper<SoulSoup> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("rand()").last("limit 1");
        SoulSoup soulSoup = this.getOne(queryWrapper);
        return soulSoup.getContent();
    }
}




