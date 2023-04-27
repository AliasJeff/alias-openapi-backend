package com.alias.openapiservice.mapper;

import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapiservice.common.PageRequest;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author zhexun
* @description 针对表【interface_info(接口信息)】的数据库操作Mapper
* @createDate 2023-04-08 23:12:03
* @Entity generator.domain.InterfaceInfo
*/
public interface InterfaceInfoMapper extends MPJBaseMapper<InterfaceInfo> {

    @Select("SELECT ii.id AS `id`, ii.name AS `name`, ii.description AS `description`, ii.method AS `method`, ii.request_params AS `requestParams`, ii.request_header AS `requestHeader`, ii.price AS `price`, ii.url AS `url`, ui.left_num AS `leftNum` FROM user_interface_info ui LEFT JOIN interface_info ii ON ii.id = ui.interface_info_id WHERE ui.user_id = #{userId} AND ui.left_num > 0 AND ui.is_delete = 0 AND (ii.is_delete = 0 OR ii.is_delete IS NULL) ${ew.customSqlSegment}")
    IPage<InterfaceInfo> getInterfaceInfoByUserId(Page<InterfaceInfo> page, @Param("userId") Long userId, @Param(Constants.WRAPPER) Wrapper<InterfaceInfo> wrapper);

}




