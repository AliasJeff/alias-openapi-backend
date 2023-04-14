package com.alias.openapiservice.service.impl;

import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alias.openapiservice.mapper.UserInterfaceInfoMapper;
import com.alias.openapiservice.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
* @author zhexun
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2023-04-12 14:14:30
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getLeftNum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用次数不足");
        }
    }

    /**
     * 调用次数计数
     * @param interfaceInfoId 接口信息ID
     * @param userId 用户ID
     * @return boolean
     */
    public boolean invokeCount(long interfaceInfoId, long userId) {
        // 判断参数是否合法
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据ID获取接口信息对象
        UserInterfaceInfo userInterfaceInfo = this.getById(interfaceInfoId);
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 获取锁定的接口信息对象，确保多个线程同时执行invokeCount方法时，只有一个线程能够修改对象，其他线程需要等待锁释放后才能进行修改
        UserInterfaceInfo lockedUserInterfaceInfo = this.getBaseMapper().selectByIdForUpdate(interfaceInfoId);
        if (lockedUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }
        // 判断剩余次数是否足够
        if (lockedUserInterfaceInfo.getLeftNum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用次数不足");
        }
        // 构造UpdateWrapper对象，设置更新条件和更新内容
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interface_info_id", interfaceInfoId);
        updateWrapper.eq("user_id", userId);
        updateWrapper.setSql("left_num = left_num - 1, total_num = total_num + 1");
        // 执行更新操作
        return this.update(updateWrapper);
    }
}
