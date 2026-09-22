/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.UserSexEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.base.util.GoogleAuthUtil;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.base.util.domain.vo.GoogleAuthVO;
import com.iwindplus.integr.api.dto.OssUploadFileDTO;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.mgt.api.upms.vo.OrgBaseCheckedVO;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgSaveUserDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditMailDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditPasswordByMailDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditPasswordByMobileDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditPasswordDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSaveByBindGrantDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSaveEditDTO;
import com.iwindplus.mgt.application.service.upms.user.vo.UserBindResultVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.PositionRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserDepartmentRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupUserRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserOrgRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserPositionRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRoleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserApplicationService {

    private final PasswordEncoder passwordEncoder;
    private final RedissonExecutor redissonExecutor;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserOrgRepository userOrgRepository;
    private final UserGroupUserRepository userGroupUserRepository;
    private final UserPositionRepository userPositionRepository;
    private final UserDepartmentRepository userDepartmentRepository;
    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;
    private final OrgRepository orgRepository;
    private final OssClient ossClient;
    private final MailCaptchaLogClient mailCaptchaLogClient;
    private final SmsCaptchaLogClient smsCaptchaLogClient;
    private final MgtProperty property;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean save(UserSaveEditDTO entity) {
        Long orgId = entity.getOrgId();

        final UserDO model = BeanUtil.copyProperties(entity, UserDO.class);
        this.buildUser(model);
        this.userRepository.save(model);
        Long userId = model.getId();
        entity.setId(userId);

        Set<Long> userIds = Set.of(userId);
        this.userOrgRepository.saveBatchUser(orgId, userIds);
        Set<Long> roleIds = this.roleRepository.listDefaultRoles(orgId);
        this.userRoleRepository.saveBatchRole(userId, roleIds);
        this.userPositionRepository.saveBatchPosition(userId, entity.getPositionIds());
        final Set<Long> departmentIds = this.positionRepository.getDepartmentIdsByPositionIds(entity.getPositionIds());
        this.userDepartmentRepository.saveBatchDepartment(userId, departmentIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean saveOrgUser(OrgSaveUserDTO entity) {
        Long userId = entity.getUserId();
        Long orgId = entity.getOrgId();
        this.userRepository.getUserIdIsNotExist(userId);

        Set<Long> userIds = Set.of(userId);
        this.userOrgRepository.saveBatchUser(orgId, userIds);
        Set<Long> roleIds = this.roleRepository.listDefaultRoles(orgId);
        this.userRoleRepository.saveBatchRole(userId, roleIds);
        this.userPositionRepository.saveBatchPosition(userId, entity.getPositionIds());
        final Set<Long> departmentIds = this.positionRepository.getDepartmentIdsByPositionIds(entity.getPositionIds());
        this.userDepartmentRepository.editBatchDepartment(userId, departmentIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<UserDO> list = this.userRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().anyMatch(UserDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.userRoleRepository.getBaseMapper().deleteByUserIds(ids);
        this.userOrgRepository.getBaseMapper().deleteByUserIds(ids);
        this.userPositionRepository.getBaseMapper().deleteByUserIds(ids);
        this.userDepartmentRepository.getBaseMapper().deleteByUserIds(ids);
        this.userGroupUserRepository.getBaseMapper().deleteByUserIds(ids);
        this.userRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean edit(UserSaveEditDTO entity) {
        UserDO data = this.userRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        final UserDO model = BeanUtil.copyProperties(entity, UserDO.class);
        this.buildUser(model, data);
        this.userRepository.updateById(model);
        this.removeOldPic(entity, data);
        this.userPositionRepository.editBatchPosition(entity.getId(), entity.getPositionIds());
        final Set<Long> departmentIds = this.positionRepository.getDepartmentIdsByPositionIds(entity.getPositionIds());
        this.userDepartmentRepository.editBatchDepartment(entity.getId(), departmentIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public UserBindResultVO editBindByMobile(UserSaveByBindGrantDTO entity) {
        final UserDO data = this.userRepository.getOne(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getMobile, entity.getMobile().trim()));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.editUser(entity, data);
        this.removeOldPic(entity, data);
        return UserBindResultVO.builder()
            .userId(data.getId())
            .bindMobileFlag(Objects.nonNull(data.getMobile()))
            .build();
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editEnabled(Long id, Boolean enabled) {
        UserDO data = this.userRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (enabled.equals(data.getEnabled())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        UserDO param = new UserDO();
        param.setId(id);
        param.setEnabled(enabled);
        param.setVersion(data.getVersion());
        this.userRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        UserDO data = this.userRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        UserDO param = new UserDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.userRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editPassword(EditPasswordDTO entity) {
        UserDO data = this.userRepository.getById(entity.getUserId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }

        boolean matches = this.passwordEncoder.matches(entity.getOldPassword(), data.getPassword());
        // 判断原密码是否正确
        if (Boolean.FALSE.equals(matches)) {
            throw new BizException(MgtCodeEnum.OLD_PASSWORD_ERROR);
        }
        return this.userRepository.editNewPassword(data, entity.getNewPassword());
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editPasswordByMobile(EditPasswordByMobileDTO entity) {
        UserDO data = this.userRepository.getOne(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getMobile, entity.getMobile().trim()));
        if (Objects.isNull(data)) {
            throw new BizException(MgtCodeEnum.MOBILE_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        final Long userId = data.getId();
        final Long orgId = this.orgRepository.getOrgId(data.getId());
        this.smsCaptchaLogClient.validateByUserId(this.property.getSms().getTplCode(), userId, orgId, entity.getCaptcha().trim());
        return this.userRepository.editNewPassword(data, entity.getNewPassword());
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editPasswordByMail(EditPasswordByMailDTO entity) {
        UserDO data = this.userRepository.getOne(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getMail, entity.getMail().trim()));
        if (Objects.isNull(data)) {
            throw new BizException(MgtCodeEnum.MAIL_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        final Long userId = data.getId();
        final Long orgId = this.orgRepository.getOrgId(data.getId());
        this.mailCaptchaLogClient.validateByUserId(this.property.getMail().getTplCode(), userId, orgId, entity.getCaptcha());
        return this.userRepository.editNewPassword(data, entity.getNewPassword());
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editMail(EditMailDTO entity) {
        UserDO data = this.userRepository.getById(entity.getUserId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验邮件是否已经绑定过了
        this.userRepository.getMailIsExist(entity.getMail().trim());

        final Long userId = data.getId();
        final Long orgId = this.orgRepository.getOrgId(data.getId());
        this.mailCaptchaLogClient.validateByUserId(this.property.getMail().getTplCode(), userId, orgId, entity.getCaptcha());

        data.setMail(entity.getMail().trim());
        this.userRepository.updateById(data);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editExitOrg(Long userId) {
        List<Long> userIds = List.of(userId);
        this.userOrgRepository.getBaseMapper().deleteByUserIds(userIds);
        this.userRoleRepository.getBaseMapper().deleteByUserIds(userIds);
        this.userPositionRepository.getBaseMapper().deleteByUserIds(userIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editChangeOrg(Long userOrgId, Long userId) {
        List<Boolean> resultList = new ArrayList<>(10);
        final OrgBaseCheckedVO org = this.orgRepository.getOrg(userId);
        if (Objects.nonNull(org) && !userOrgId.equals(org.getUserOrgId())) {
            resultList.add(this.userOrgRepository.editChecked(userOrgId, org.getUserOrgId()));
        }
        if (CollUtil.isNotEmpty(resultList)) {
            return resultList.stream().anyMatch(Boolean.TRUE::equals);
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editGaBindFlag(Long userId, String captcha) {
        final UserDO data = this.userRepository.getById(userId);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (Boolean.TRUE.equals(data.getGaBindFlag())) {
            throw new BizException(BizCodeEnum.GA_ALREADY_BOUND);
        }
        final boolean result = GoogleAuthUtil.validate(data.getGaSecret(), Integer.parseInt(captcha));
        if (Boolean.FALSE.equals(result)) {
            throw new BizException(BizCodeEnum.GA_CAPTCHA_ERROR);
        }
        UserDO param = new UserDO();
        param.setId(data.getId());
        param.setVersion(data.getVersion());
        param.setGaBindFlag(Boolean.TRUE);
        this.userRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
        }
    )
    public boolean editResetGa(Long userId) {
        final UserDO data = this.userRepository.getById(userId);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (ObjectUtil.isEmpty(data.getGaSecret()) && Boolean.FALSE.equals(data.getGaBindFlag())) {
            throw new BizException(BizCodeEnum.GA_ALREADY_RESET);
        }
        UserDO param = new UserDO();
        param.setId(data.getId());
        param.setVersion(data.getVersion());
        param.setGaSecret("");
        param.setGaBindFlag(Boolean.FALSE);
        this.userRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {"user"}, allEntries = true),
            @CacheEvict(cacheNames = {"department"}, allEntries = true),
            @CacheEvict(cacheNames = {"userGroup"}, allEntries = true),
            @CacheEvict(cacheNames = {"org"}, allEntries = true),
            @CacheEvict(cacheNames = {"position"}, allEntries = true),
            @CacheEvict(cacheNames = {"resource"}, allEntries = true),
            @CacheEvict(cacheNames = {"role"}, allEntries = true),
            @CacheEvict(cacheNames = {"roleMenu"}, allEntries = true),
            @CacheEvict(cacheNames = {"roleResource"}, allEntries = true),
            @CacheEvict(cacheNames = {"positionRole"}, allEntries = true),
        }
    )
    public GoogleAuthVO getGaQrcode(Long userId, Integer width, Integer height) {
        final UserDO data = this.userRepository.getById(userId);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (ObjectUtil.isNotEmpty(data.getGaSecret()) && data.getGaBindFlag()) {
            throw new BizException(BizCodeEnum.GA_ALREADY_BOUND);
        }
        String serverId = SpringUtil.getApplicationName();
        String profile = SpringUtil.getActiveProfile();
        final StringBuilder sb = new StringBuilder(serverId);
        if (ObjectUtil.isNotEmpty(profile)) {
            sb.append(SymbolConstant.HORIZONTAL_LINE).append(profile);
        }
        String issuer = sb.toString();
        final GoogleAuthVO result = GoogleAuthUtil.generateImage(issuer, data.getUsername(), width, height);
        if (ObjectUtil.isEmpty(result)) {
            throw new BizException(BizCodeEnum.GA_QRCODE_GENERATE_ERROR);
        }
        UserDO param = new UserDO();
        param.setId(data.getId());
        param.setVersion(data.getVersion());
        param.setGaSecret(result.getKey());
        this.userRepository.updateById(param);
        return result;
    }

    private void editUser(UserSaveByBindGrantDTO entity, UserDO data) {
        if (CharSequenceUtil.isBlank(entity.getNickName())
            && Objects.isNull(entity.getSex())
            && CharSequenceUtil.isBlank(entity.getCountry())
            && CharSequenceUtil.isBlank(entity.getProvince())
            && CharSequenceUtil.isBlank(entity.getCity())
            && CharSequenceUtil.isBlank(entity.getAvatar())) {
            return;
        }
        final UserDO param = new UserDO();
        param.setId(data.getId());
        if (CharSequenceUtil.isNotBlank(entity.getNickName())) {
            param.setNickName(entity.getNickName());
        }
        if (Objects.nonNull(entity.getSex())) {
            param.setSex(entity.getSex());
        }
        try {
            UserDO user = this.buildAvatar(entity);
            if (Objects.nonNull(user)) {
                param.setAvatar(user.getAvatar());
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        param.setVersion(data.getVersion());
        this.userRepository.updateById(param);
    }

    private UserDO buildAvatar(UserSaveByBindGrantDTO entity) {
        final String code = this.property.getOss().getCode();
        String ossTplCode = this.property.getOss().getTplCode();
        String avatar = entity.getAvatar();
        if (CharSequenceUtil.isBlank(code) || CharSequenceUtil.isBlank(ossTplCode) || CharSequenceUtil.isBlank(avatar)) {
            return null;
        }
        String relativePath = avatar.concat(FileTypeEnum.PNG.getSuffix());
        OssUploadFileDTO condition = OssUploadFileDTO.builder()
            .code(code)
            .tplCode(ossTplCode)
            .url(entity.getAvatar())
            .relativePath(relativePath)
            .sourceFileName(FileUtil.getName(entity.getAvatar()))
            .contentType(FileTypeEnum.PNG.getContentType())
            .build();
        UploadVO uploadVO = Optional.ofNullable(this.ossClient.uploadFile(condition)).map(ResultVO::getBizData).orElse(null);
        UserDO param = new UserDO();
        param.setAvatar(Optional.ofNullable(uploadVO).map(UploadVO::getRelativePath).orElse(null));
        return param;
    }

    private void buildUser(UserDO entity) {
        entity.setEnabled(Boolean.TRUE);
        entity.setBuildInFlag(Boolean.FALSE);
        if (Objects.isNull(entity.getSex())) {
            entity.setSex(UserSexEnum.UNKNOWN);
        }
        // 校验用户名是否存在
        this.userRepository.getUsernameIsExist(entity.getUsername().trim());
        // 校验手机是否存在
        this.userRepository.getMobileIsExist(entity.getMobile().trim());
        // 校验邮箱是否存在
        if (CharSequenceUtil.isNotBlank(entity.getMail())) {
            this.userRepository.getMailIsExist(entity.getMail().trim());
        }
        // 校验身份证是否存在
        if (CharSequenceUtil.isNotBlank(entity.getIdCard())) {
            this.buildIdCard(entity);
        }

        // 设置默认密码
        if (CharSequenceUtil.isBlank(entity.getPassword())) {
            // 默认密码为手机（手机后6位）
            String password = CharSequenceUtil.subSuf(entity.getMobile(), entity.getMobile().trim().length() - 6);
            entity.setPassword(password);
        }
        // 密码加密
        if (CharSequenceUtil.isNotBlank(entity.getPassword())) {
            entity.setPassword(this.passwordEncoder.encode(entity.getPassword()));
        }

        entity.setJobNumber(this.redissonExecutor.serialNum().getSerialNum("U", NumberConstant.NUMBER_THREE, false));
        this.userRepository.getJobNumberIsExist(entity.getJobNumber().trim());
    }

    private void buildUser(UserDO entity, UserDO data) {
        entity.setJobNumber(null);
        entity.setPassword(null);
        entity.setUsername(null);
        // 校验工号是否存在
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber()) && !CharSequenceUtil.equals(data.getJobNumber(), entity.getJobNumber().trim())) {
            this.userRepository.getJobNumberIsExist(entity.getJobNumber().trim());
        }
        // 校验手机是否存在
        if (CharSequenceUtil.isNotBlank(entity.getMobile()) && !CharSequenceUtil.equals(data.getMobile(), entity.getMobile().trim())) {
            this.userRepository.getMobileIsExist(entity.getMobile().trim());
        }
        // 校验邮箱是否存在
        if (CharSequenceUtil.isNotBlank(entity.getMail()) && !CharSequenceUtil.equals(data.getMail(), entity.getMail().trim())) {
            this.userRepository.getMailIsExist(entity.getMail().trim());
        }
        // 校验身份证是否存在
        if (CharSequenceUtil.isNotBlank(entity.getIdCard()) && !CharSequenceUtil.equals(data.getIdCard(), entity.getIdCard().trim())) {
            this.buildIdCard(entity);
        }

        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
    }

    private void buildIdCard(UserDO entity) {
        if (!IdcardUtil.isValidCard(entity.getIdCard().trim())) {
            throw new BizException(MgtCodeEnum.ID_CARD_FORMAT_ERROR);
        }
        this.userRepository.getIdCardIsExist(entity.getIdCard().trim());
        entity.setBirthday(IdcardUtil.getBirthByIdCard(entity.getIdCard().trim()));
        int gender = IdcardUtil.getGenderByIdCard(entity.getIdCard().trim());
        if (gender == 0) {
            entity.setSex(UserSexEnum.FEMALE);
        } else if (gender == 1) {
            entity.setSex(UserSexEnum.MALE);
        }
    }

    private void removeOldPic(UserSaveEditDTO entity, UserDO data) {
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(entity.getAvatar())
            && CharSequenceUtil.isNotBlank(data.getAvatar())
            && !CharSequenceUtil.equals(data.getAvatar(), entity.getAvatar().trim())) {
            relativePaths.add(data.getAvatar());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIdCardFront())
            && CharSequenceUtil.isNotBlank(data.getIdCardFront())
            && !CharSequenceUtil.equals(data.getIdCardFront(), entity.getIdCardFront().trim())) {
            relativePaths.add(data.getIdCardFront());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIdCardBack())
            && CharSequenceUtil.isNotBlank(data.getIdCardBack())
            && !CharSequenceUtil.equals(data.getIdCardBack(), entity.getIdCardBack().trim())) {
            relativePaths.add(data.getIdCardBack());
        }
        OrgApplicationService.removeFiles(this.ossClient,
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths);
    }

}
