package com.alias.openapiservice.mapper;

import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author zhexun
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-04-12 14:14:30
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

    @Select("SELECT * FROM user_interface_info WHERE id = #{id} FOR UPDATE")
    UserInterfaceInfo selectByIdForUpdate(Long id);
}




