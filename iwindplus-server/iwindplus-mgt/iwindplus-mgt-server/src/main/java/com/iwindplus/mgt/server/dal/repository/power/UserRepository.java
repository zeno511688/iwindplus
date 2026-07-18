/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.power.UserMapper;
import com.iwindplus.mgt.server.dal.model.power.UserDO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
@RequiredArgsConstructor
public class UserRepository extends JoinCrudRepository<UserMapper, UserDO> {

    private final PasswordEncoder passwordEncoder;

    /**
     * 修改用户密码
     *
     * @param data        用户信息
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editNewPassword(UserDO data, String newPassword) {
        // 判断新密码是否跟原密码一致
        boolean matches = this.passwordEncoder.matches(newPassword, data.getPassword());
        if (Boolean.TRUE.equals(matches)) {
            throw new BizException(MgtCodeEnum.PASSWORD_COMMON);
        }
        UserDO param = new UserDO();
        param.setId(data.getId());
        param.setPassword(this.passwordEncoder.encode(newPassword));
        param.setVersion(data.getVersion());
        super.updateById(param);
        return Boolean.TRUE;
    }

    /**
     * 校验用户主键是否不存在
     *
     * @param userId 用户主键
     */
    public void getUserIdIsNotExist(Long userId) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getId, userId)
            .select(UserDO::getId)));
        if (Boolean.FALSE.equals(result)) {
            throw new BizException(MgtCodeEnum.USER_NOT_EXIST);
        }
    }

    /**
     * 获取用户工号是否已存在
     *
     * @param jobNumber 工号
     */
    public void getJobNumberIsExist(String jobNumber) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getJobNumber, jobNumber)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.JOB_NUMBER_EXIST);
        }
    }

    /**
     * 获取用户账号是否已存在
     *
     * @param username 账号
     */
    public void getUsernameIsExist(String username) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getUsername, username)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.USERNAME_EXIST);
        }
    }

    /**
     * 获取用户手机号是否已存在
     *
     * @param mobile 手机号
     */
    public void getMobileIsExist(String mobile) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getMobile, mobile)));
        if (result) {
            throw new BizException(MgtCodeEnum.MOBILE_EXIST);
        }
    }

    /**
     * 获取用户邮箱是否已存在
     *
     * @param mail 邮箱
     */
    public void getMailIsExist(String mail) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getMail, mail)));
        if (result) {
            throw new BizException(MgtCodeEnum.MAIL_EXIST);
        }
    }

    /**
     * 获取用户身份证号是否已存在
     *
     * @param idCard 身份证号
     */
    public void getIdCardIsExist(String idCard) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getIdCard, idCard)));
        if (result) {
            throw new BizException(MgtCodeEnum.ID_CARD_EXIST);
        }
    }
}
