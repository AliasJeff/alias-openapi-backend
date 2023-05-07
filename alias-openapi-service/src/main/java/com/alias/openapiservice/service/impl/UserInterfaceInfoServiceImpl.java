package com.alias.openapiservice.service.impl;

import com.alias.openapicommon.model.entity.InterfaceInfo;
import com.alias.openapicommon.model.entity.User;
import com.alias.openapiservice.common.ErrorCode;
import com.alias.openapiservice.common.PageRequest;
import com.alias.openapiservice.constant.CommonConstant;
import com.alias.openapiservice.exception.BusinessException;
import com.alias.openapicommon.model.entity.UserInterfaceInfo;
import com.alias.openapiservice.mapper.InterfaceInfoMapper;
import com.alias.openapiservice.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.alias.openapiservice.service.InterfaceInfoService;
import com.alias.openapiservice.service.UserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alias.openapiservice.mapper.UserInterfaceInfoMapper;
import com.alias.openapiservice.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhexun
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-04-12 14:14:30
 */
@Service
@Slf4j
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private UserService userService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    /**
     * todo 优化代码
     *
     * 性能问题：可以通过在查询用户已有的接口调用记录时，使用 SQL 的 join 操作，一次性查询出所有用户和接口的关联关系，避免多次查询数据库。同时，可以将查询出的结果缓存到内存中，避免重复查询。
     *
     * 内存占用问题：可以采用分批次处理的方式，每次只处理一部分用户和接口信息，避免一次性将所有数据加载到内存中。同时，可以使用游标或者流式处理的方式，避免一次性将所有数据加载到内存中。
     *
     * 事务问题：可以将整个方法的事务注解移到方法的外层，避免事务嵌套问题。如果需要对每个用户的操作单独进行事务控制，可以将每个用户的操作封装为一个独立的方法，然后在外层方法中调用这些方法，分别进行事务控制。
     *
     * 代码复杂度问题：可以将代码拆分为多个方法，每个方法只负责一个功能，避免嵌套层次过深。同时，可以使用注解或者配置文件的方式，将一些配置信息（如分页大小、重试次数等）提取到外部，避免硬编码。
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 使用数据库的事务保证数据的一致性
    public void addUserInterfaceInfo() {
        // 使用分页查询，每次查询一定数量的数据，减少数据库的负担
        int pageSize = 100;
        int pageNum = 1;
        List<User> allUsers = new ArrayList<>();
        List<InterfaceInfo> allInterfaces = new ArrayList<>();
        List<UserInterfaceInfo> userInterfaces = new ArrayList<>();

        IPage<InterfaceInfo> interfaceInfoPage = null;
        do {
            interfaceInfoPage = interfaceInfoService.page(new Page<>(pageNum, pageSize));
            allInterfaces.addAll(interfaceInfoPage.getRecords());
            pageNum++;
        } while (pageNum <= interfaceInfoPage.getPages());

        pageNum = 1;
        IPage<User> userIPage = null;
        do {
            userIPage = userService.page(new Page<>(pageNum, pageSize));
            allUsers.addAll(userIPage.getRecords());
            pageNum++;
        } while (pageNum <= userIPage.getPages());

        // 遍历所有用户
        for (User user : allUsers) {
            long userId = user.getId();

            userInterfaces = new ArrayList<>();
            List<UserInterfaceInfo> newUserInterfaces = new ArrayList<>();

            pageNum = 1;
            IPage<UserInterfaceInfo> userInterfaceInfoIPage = null;
            QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            do {
                userInterfaceInfoIPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
                userInterfaces.addAll(userInterfaceInfoIPage.getRecords());
                pageNum++;
            } while (pageNum <= userInterfaceInfoIPage.getPages());

            for (InterfaceInfo interfaceInfo : allInterfaces) {
                boolean flag = false;

                // 判断用户是否已有该接口
                for (UserInterfaceInfo userInterfaceInfo : userInterfaces) {
                    if (userInterfaceInfo.getInterfaceInfoId().equals(interfaceInfo.getId())) {
                        flag = true;
                        break;
                    }
                }

                // 如果用户没有该接口，则新增一条数据
                if (!flag) {
                    UserInterfaceInfo newUserInterface = new UserInterfaceInfo();
                    newUserInterface.setUserId(userId);
                    newUserInterface.setInterfaceInfoId(interfaceInfo.getId());
                    newUserInterface.setLeftNum(20);
                    newUserInterfaces.add(newUserInterface);
                }
            }

            // 批量插入用户接口调用次数记录，使用数据库事务保证数据的一致性
            if (newUserInterfaces != null && newUserInterfaces.size() > 0) {
                this.saveBatch(newUserInterfaces);
            }

            // 遍历用户接口调用次数记录，如果对应的接口已被删除，则删除该记录
            for (UserInterfaceInfo userInterfaceInfo : userInterfaces) {
                boolean flag = false;
                for (InterfaceInfo interfaceInfo : allInterfaces) {
                    if (userInterfaceInfo.getInterfaceInfoId().equals(interfaceInfo.getId())) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    this.removeById(userInterfaceInfo.getId());
                }
            }
        }
    }


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

        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interface_info_id", userInterfaceInfo.getInterfaceInfoId());
        queryWrapper.eq("user_id", userInterfaceInfo.getUserId());
        UserInterfaceInfo one = this.getOne(queryWrapper);

        if (userInterfaceInfo.getLeftNum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用次数不足");
        }
    }

    /**
     * 调用次数计数
     *
     * @param interfaceInfoId 接口信息ID
     * @param userId          用户ID
     * @return boolean
     */
    public boolean invokeCount(long interfaceInfoId, long userId) {
        // 判断参数是否合法
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据ID获取接口信息对象
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interface_info_id", interfaceInfoId);
        queryWrapper.eq("user_id", userId);
        UserInterfaceInfo userInterfaceInfo = this.getOne(queryWrapper);
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }
        // 判断剩余次数是否足够
        if (userInterfaceInfo.getLeftNum() <= 0) {
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

    @Override
    @Transactional
    public IPage<InterfaceInfo> getAvailableInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest, long userId) {
        if (userId == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();

        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(current);
        pageRequest.setPageSize(size);
        pageRequest.setSortField(interfaceInfoQueryRequest.getSortField());
        pageRequest.setSortOrder(interfaceInfoQueryRequest.getSortOrder());

        Page<InterfaceInfo> page = new Page<>(current, size);
        QueryWrapper<InterfaceInfo> wrapper = new QueryWrapper<>();

        return interfaceInfoMapper.getInterfaceInfoByUserId(page, userId, wrapper);
    }
}
