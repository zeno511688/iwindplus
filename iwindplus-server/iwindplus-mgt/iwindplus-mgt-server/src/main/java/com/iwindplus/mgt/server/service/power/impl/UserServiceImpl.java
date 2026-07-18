/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.BeanConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.UserSexEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO.UserExtendFunctionValidVOBuilder;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.CheckDataUtil;
import com.iwindplus.base.util.GoogleAuthUtil;
import com.iwindplus.base.util.YubikeyUtil;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.base.util.domain.vo.GoogleAuthVO;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.EditMailDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordByMailDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordByMobileDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSaveUserDTO;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.dto.power.UserSaveByThirdDTO;
import com.iwindplus.mgt.domain.dto.power.UserSaveEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.domain.vo.power.UserBindResultVO;
import com.iwindplus.mgt.domain.vo.power.UserDepartmentInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserDetailVO;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserLoginExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserLoginVO;
import com.iwindplus.mgt.domain.vo.power.UserOrgInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserPageVO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import com.iwindplus.mgt.server.config.property.MgtProperty;
import com.iwindplus.mgt.server.dal.model.power.UserDO;
import com.iwindplus.mgt.server.dal.model.power.UserExtendYubikeyDO;
import com.iwindplus.mgt.server.dal.repository.power.OrgRepository;
import com.iwindplus.mgt.server.dal.repository.power.PositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.ResourceRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserDepartmentRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserExtendYubikeyRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupUserRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserOrgRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserPositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserRoleRepository;
import com.iwindplus.mgt.server.service.power.UserService;
import com.iwindplus.setup.client.OssClient;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final RedissonService redissonService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserOrgRepository userOrgRepository;
    private final UserGroupUserRepository userGroupUserRepository;
    private final UserPositionRepository userPositionRepository;
    private final UserDepartmentRepository userDepartmentRepository;
    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final OrgRepository orgRepository;
    private final OssClient ossClient;
    private final MailCaptchaLogClient mailCaptchaLogClient;
    private final SmsCaptchaLogClient smsCaptchaLogClient;
    private final MgtProperty property;
    private final UserExtendYubikeyRepository userExtendYubikeyRepository;

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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public UserBindResultVO editBindByMobile(UserSaveByThirdDTO entity) {
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public IPage<UserPageVO> page(UserSearchDTO entity) {
        final PageDTO<UserPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.userRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<UserVO> listByCondition(String param) {
        final List<UserDO> list = this.userRepository.list(Wrappers.lambdaQuery(UserDO.class)
            .eq(UserDO::getJobNumber, param.trim())
            .or()
            .eq(UserDO::getMobile, param.trim())
            .or()
            .eq(UserDO::getRealName, param.trim())
            .or()
            .eq(UserDO::getMail, param.trim())
            .or()
            .eq(UserDO::getUsername, param.trim())
            .or()
            .eq(UserDO::getIdCard, param.trim()));
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, UserVO.class);
    }

    @Cacheable(value = {"user::listInfoByIds"}, keyGenerator = BeanConstant.BEAN_KEY_GENERATOR, unless = "#result == null")
    @Override
    public List<UserVO> listInfoByIds(List<Long> ids) {
        CheckDataUtil.checkBatchOperationSize(ids.size(), NumberConstant.NUMBER_FIFTY);
        final List<UserDO> list = this.userRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyToList(list, UserVO.class);
    }

    @Override
    public List<UserExtendVO> listExtendByIds(List<Long> ids) {
        CheckDataUtil.checkBatchOperationSize(ids.size(), NumberConstant.NUMBER_FIFTY);
        final List<UserVO> list = this.listInfoByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<UserExtendVO> voList = BeanUtil.copyToList(list, UserExtendVO.class);
        final List<String> relativePaths = new ArrayList<>(10);
        voList.stream().forEach(data -> {
            if (CharSequenceUtil.isNotBlank(data.getAvatar())) {
                relativePaths.add(data.getAvatar());
            }
            if (CharSequenceUtil.isNotBlank(data.getIdCardFront())) {
                relativePaths.add(data.getIdCardFront());
            }
            if (CharSequenceUtil.isNotBlank(data.getIdCardBack())) {
                relativePaths.add(data.getIdCardBack());
            }
        });
        List<FilePathVO> filePaths = OrgServiceImpl.getFilePaths(this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        return voList.stream().map(data -> {
            this.buildImage(filePaths, data, data.getAvatar(), data.getIdCardFront(), data.getIdCardBack());
            return data;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<UserOrgInfoVO> listByRoleIds(List<Long> roleIds) {
        return this.userRepository.getBaseMapper().selectListByRoleIds(roleIds);
    }

    @Override
    public List<UserDepartmentInfoVO> listByDepartmentIds(List<Long> departmentIds) {
        return this.userRepository.getBaseMapper().selectListByDepartmentIds(departmentIds);
    }

    @Override
    public UserExtendFunctionValidVO checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity) {
        final UserExtendFunctionValidVOBuilder<?, ?> builder = UserExtendFunctionValidVO.builder();
        final Long userId = entity.getUserId();
        final Long orgId = entity.getOrgId();
        final UserDO data = this.userRepository.getById(userId);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        // 校验GA验证码
        validateGa(entity, builder, data);
        // 校验邮箱验证码
        validateMail(entity, builder, data, userId, orgId);
        // 校验短信验证码
        validateSms(entity, builder, data, userId, orgId);
        // 校验yubikey
        validateYubikey(entity, builder, userId);

        return builder.build();
    }

    @Cacheable(value = {"user::getByCondition"}, keyGenerator = BeanConstant.BEAN_KEY_GENERATOR, unless = "#result == null")
    @Override
    public UserVO getByCondition(UserBaseQueryDTO entity) {
        final UserDO data = this.userRepository.getOne(Wrappers.lambdaQuery(UserDO.class)
            .eq(ObjectUtil.isNotEmpty(entity.getJobNumber()), UserDO::getUsername, entity.getJobNumber())
            .eq(ObjectUtil.isNotEmpty(entity.getUsername()), UserDO::getUsername, entity.getUsername())
            .eq(ObjectUtil.isNotEmpty(entity.getMobile()), UserDO::getUsername, entity.getMobile())
            .eq(ObjectUtil.isNotEmpty(entity.getMail()), UserDO::getUsername, entity.getMail())
            .eq(ObjectUtil.isNotEmpty(entity.getIdCard()), UserDO::getUsername, entity.getIdCard())
            .eq(UserDO::getEnabled, Boolean.TRUE));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, UserVO.class);
    }

    @Cacheable(value = {"user::getLoginInfoByCondition"}, keyGenerator = BeanConstant.BEAN_KEY_GENERATOR, unless = "#result == null")
    @Override
    public UserInfoVO getLoginInfoByCondition(UserBaseQueryDTO entity) {
        UserInfoVO userInfo = this.userRepository.getBaseMapper().selectByCondition(entity);
        if (Objects.isNull(userInfo)) {
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return userInfo;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public UserDetailVO getLoginByParam(String param) {
        UserDetailVO data = this.userRepository.getBaseMapper().selectLoginByParam(param);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return this.getCurrentUserInfoVO(data);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public UserDetailVO getLoginByCode(String code) {
        UserDetailVO data = this.userRepository.getBaseMapper().selectLoginByCode(code);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.CODE_NOT_EXIST);
        }
        return this.getCurrentUserInfoVO(data);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public UserVO getDetail(Long id) {
        UserDO data = this.userRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, UserVO.class);
    }

    @Override
    public UserExtendVO getDetailExtend(Long id) {
        UserVO data = this.getDetail(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        UserExtendVO result = BeanUtil.copyProperties(data, UserExtendVO.class);
        if (CharSequenceUtil.isNotBlank(result.getIdCard())) {
            result.setAge(IdcardUtil.getAgeByIdCard(result.getIdCard()));
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getAvatar())) {
            relativePaths.add(data.getAvatar());
        }
        if (CharSequenceUtil.isNotBlank(data.getIdCardFront())) {
            relativePaths.add(data.getIdCardFront());
        }
        if (CharSequenceUtil.isNotBlank(data.getIdCardBack())) {
            relativePaths.add(data.getIdCardBack());
        }
        List<FilePathVO> filePaths = OrgServiceImpl.getFilePaths(this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        this.buildImage(filePaths, result, data.getAvatar(), data.getIdCardFront(), data.getIdCardBack());
        return result;
    }

    @Override
    public UserLoginExtendVO getUserExtendInfo(Long orgId, Long userId) {
        final UserLoginVO userInfo = this.getUserInfo(orgId, userId);
        if (ObjectUtil.isEmpty(userInfo)) {
            return null;
        }
        final UserLoginExtendVO data = BeanUtil.copyProperties(userInfo, UserLoginExtendVO.class);
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getAvatar())) {
            relativePaths.add(data.getAvatar());
        }

        List<FilePathVO> filePaths = OrgServiceImpl.getFilePaths(this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        if (CollUtil.isEmpty(filePaths)) {
            return data;
        }
        filePaths.stream().forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getAvatar()) && data.getAvatar().equals(p.getRelativePath())) {
                data.setAvatarStr(p.getAbsolutePath());
            }
        });
        return data;
    }

    @Override
    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null and #p1 != null", unless = "#result == null")
    public UserLoginVO getUserInfo(Long orgId, Long userId) {
        if (Boolean.FALSE.equals(this.userOrgRepository.checkChangeOrg(userId, orgId))) {
            throw new BizException(MgtCodeEnum.USER_HAS_SWITCHED_ORG);
        }
        final UserInfoVO userInfo = this.userRepository.getBaseMapper().selectByUserId(userId);
        if (Objects.isNull(userInfo)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final UserLoginVO data = BeanUtil.copyProperties(userInfo, UserLoginVO.class);
        if (CharSequenceUtil.isNotBlank(data.getIdCard())) {
            data.setAge(IdcardUtil.getAgeByIdCard(data.getIdCard()));
        }
        // 查询角色权限
        List<RoleBaseVO> listRolePermission = this.roleRepository.listCheckedByUserId(data.getOrgId(), userId);
        if (CollUtil.isNotEmpty(listRolePermission)) {
            final Set<RoleBaseVO> list = listRolePermission.stream().sorted(Comparator.comparing(RoleBaseVO::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
            data.setRolePermissions(list);
        }
        // 查询按钮权限
        List<ResourceBaseVO> listButtonPermission = this.resourceRepository.listButtonCheckedByUserId(orgId, userId);
        if (CollUtil.isNotEmpty(listButtonPermission)) {
            final Set<ResourceBaseVO> list = listButtonPermission.stream().sorted(Comparator.comparing(ResourceBaseVO::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
            data.setButtonPermissions(list);
        }
        return data;
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
    @Override
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

    private UserDetailVO getCurrentUserInfoVO(UserDetailVO data) {
        if (Boolean.FALSE.equals(data.getEnabled())) {
            throw new BizException(BizCodeEnum.ACCOUNT_DISABLED);
        } else if (Boolean.TRUE.equals(data.getLocked())) {
            throw new BizException(BizCodeEnum.ACCOUNT_LOCKED);
        } else if (Boolean.TRUE.equals(data.getAccountExpired())) {
            throw new BizException(BizCodeEnum.ACCOUNT_EXPIRED);
        } else if (Boolean.TRUE.equals(data.getCredentialsExpired())) {
            throw new BizException(BizCodeEnum.CREDENTIALS_EXPIRED);
        }
        // 查询角色编码
        List<RoleBaseVO> listRolePermission = this.roleRepository.listCheckedByUserId(data.getOrgId(), data.getUserId());
        if (CollUtil.isNotEmpty(listRolePermission)) {
            final Set<String> codes = listRolePermission.stream()
                .map(RoleBaseVO::getCode)
                .collect(Collectors.toCollection(HashSet::new));
            data.setPermissions(codes);
        }
        return data;
    }

    private void editUser(UserSaveByThirdDTO entity, UserDO data) {
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

    private UserDO buildAvatar(UserSaveByThirdDTO entity) {
        String ossTplCode = this.property.getOss().getTplCode();
        String avatar = entity.getAvatar();
        if (CharSequenceUtil.isBlank(ossTplCode) || CharSequenceUtil.isBlank(avatar)) {
            return null;
        }
        byte[] bytes = new byte[0];
        try {
            bytes = HttpUtil.downloadBytes(avatar);
        } catch (Exception ex) {
            log.info(ExceptionConstant.EXCEPTION, ex);
        }
        if (CharSequenceUtil.isBlank(FileUtil.getSuffix(avatar))) {
            avatar = avatar.concat(FileTypeEnum.PNG.getSuffix());
        }
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }
        final UploadByteDTO attachment = UploadByteDTO.builder()
            .data(ArrayUtil.wrap(bytes))
            .sourceFileName(FileUtil.getName(avatar))
            .contentType(FileTypeEnum.PNG.getContentType())
            .build();
        OssUploadByteDTO condition = OssUploadByteDTO.builder()
            .tplCode(ossTplCode)
            .attachment(attachment)
            .prefix("pic/user")
            .renamed(Boolean.TRUE)
            .build();
        UploadVO uploadVO = Optional.ofNullable(this.ossClient.uploadByte(condition)).map(ResultVO::getBizData).orElse(null);
        UserDO param = new UserDO();
        param.setAvatar(uploadVO.getRelativePath());
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

        entity.setJobNumber(this.redissonService.serialNum().getSerialNum("U", NumberConstant.NUMBER_THREE, false));
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
        OrgServiceImpl.removeFiles(this.ossClient, this.property.getOss().getTplCode(), relativePaths);
    }

    private void buildImage(List<FilePathVO> filePaths, UserExtendVO result, String avatar, String idCardFront, String idCardBack) {
        if (CollUtil.isNotEmpty(filePaths)) {
            filePaths.stream().forEach(p -> {
                if (CharSequenceUtil.isNotBlank(avatar) && avatar.equals(p.getRelativePath())) {
                    result.setAvatarStr(p.getAbsolutePath());
                }
                if (CharSequenceUtil.isNotBlank(idCardFront) && idCardFront.equals(p.getRelativePath())) {
                    result.setIdCardFrontStr(p.getAbsolutePath());
                }
                if (CharSequenceUtil.isNotBlank(idCardBack) && idCardBack.equals(p.getRelativePath())) {
                    result.setIdCardBackStr(p.getAbsolutePath());
                }
            });
        }
    }

    private void validateGa(
        UserExtendFunctionValidDTO entity,
        UserExtendFunctionValidVOBuilder<?, ?> builder,
        UserDO data) {

        final String gaCaptcha = entity.getGaCaptcha();
        if (ObjectUtil.isEmpty(gaCaptcha)) {
            builder.gaBindFlag(false)
                .gaCheckFlag(false);
            return;
        }

        // 检查是否为数字
        if (!CharSequenceUtil.isNumeric(gaCaptcha)) {
            throw new BizException(BizCodeEnum.ONLY_SUPPORT_NUMBER);
        }

        final boolean isBound = ObjectUtil.isNotEmpty(data.getGaSecret()) && Boolean.TRUE.equals(data.getGaBindFlag());
        if (!isBound) {
            builder.gaBindFlag(false)
                .gaCheckFlag(false);
            return;
        }

        // GA 已绑定，校验验证码
        final boolean isValid = GoogleAuthUtil.validate(data.getGaSecret(), Integer.parseInt(gaCaptcha));

        builder.gaBindFlag(true)
            .gaCheckFlag(isValid);
    }

    private void validateMail(
        UserExtendFunctionValidDTO entity,
        UserExtendFunctionValidVOBuilder<?, ?> builder,
        UserDO data,
        Long userId,
        Long orgId) {

        final String mailCaptcha = entity.getMailCaptcha();
        if (ObjectUtil.isEmpty(mailCaptcha)) {
            builder.mailBindFlag(false)
                .mailCheckFlag(false);
            return;
        }

        final String mail = data.getMail();
        final boolean isBound = ObjectUtil.isNotEmpty(mail);
        if (!isBound) {
            builder.mailBindFlag(false)
                .mailCheckFlag(false);
            return;
        }

        // 邮箱已绑定，校验验证码
        final String tplCode = this.property.getMail().getTplCode();
        final ResultVO<Boolean> result = this.mailCaptchaLogClient.validateByUserId(tplCode, userId, orgId, mailCaptcha);
        // 校验异常直接抛出
        result.errorThrow();

        final Boolean isValid = result.getBizData();

        builder.mailBindFlag(true)
            .mailCheckFlag(Boolean.TRUE.equals(isValid));
    }

    private void validateSms(
        UserExtendFunctionValidDTO entity,
        UserExtendFunctionValidVOBuilder<?, ?> builder,
        UserDO data,
        Long userId,
        Long orgId) {

        final String smsCaptcha = entity.getSmsCaptcha();
        if (ObjectUtil.isEmpty(smsCaptcha)) {
            builder.mobileBindFlag(false)
                .smsCheckFlag(false);
            return;
        }

        final String mobile = data.getMobile();
        final boolean isBound = ObjectUtil.isNotEmpty(mobile);
        if (!isBound) {
            builder.mobileBindFlag(false)
                .smsCheckFlag(false);
            return;
        }

        // 手机已绑定，校验验证码
        final String tplCode = this.property.getSms().getTplCode();
        final ResultVO<Boolean> result = this.smsCaptchaLogClient.validateByUserId(tplCode, userId, orgId, smsCaptcha);

        // 校验异常直接抛出
        result.errorThrow();

        final Boolean isValid = Boolean.TRUE.equals(result.getBizData());

        builder.mobileBindFlag(true)
            .smsCheckFlag(isValid);
    }

    private void validateYubikey(
        UserExtendFunctionValidDTO entity,
        UserExtendFunctionValidVOBuilder<?, ?> builder,
        Long userId) {

        final String yubikeySource = entity.getYubikeySource();
        final String yubikeySign = entity.getYubikeySign();
        if (ObjectUtil.isEmpty(yubikeySource) || ObjectUtil.isEmpty(yubikeySign)) {
            builder.yubikeyBindFlag(false)
                .yubikeyCheckFlag(false);
            return;
        }

        final UserExtendYubikeyDO userExtendYubikey = this.userExtendYubikeyRepository.getByUserId(userId, YubikeyBizTypeEnum.GENERAL);
        final String yubikeyPublicKey = userExtendYubikey.getYubikeyPublicKey();
        final boolean isBound = ObjectUtil.isNotEmpty(yubikeyPublicKey);
        if (!isBound) {
            builder.yubikeyBindFlag(false)
                .yubikeyCheckFlag(false);
            return;
        }

        final boolean isValid = YubikeyUtil.verifySign(yubikeyPublicKey, yubikeySource, yubikeySign);
        builder.yubikeyBindFlag(true)
            .yubikeyCheckFlag(isValid);
    }

    /**
     * 通过职位ID集合获取部门ID集合.
     *
     * @param positionIds 职位ID集合
     * @return 部门ID集合
     */
    private Set<Long> getPositionDepartmentIds(Set<Long> positionIds) {
        return this.positionRepository.getDepartmentIdsByPositionIds(positionIds);
    }

}
