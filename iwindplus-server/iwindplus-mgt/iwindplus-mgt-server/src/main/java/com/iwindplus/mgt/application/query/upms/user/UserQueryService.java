/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.BeanConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO.UserExtendFunctionValidVOBuilder;
import com.iwindplus.base.util.CheckDataUtil;
import com.iwindplus.base.util.GoogleAuthUtil;
import com.iwindplus.base.util.YubikeyUtil;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.mgt.application.query.upms.user.vo.UserLoginExtendVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserLoginVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserPageVO;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSearchDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendYubikeyDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendYubikeyRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserOrgRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.YubikeyBizTypeEnum;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseVO;
import com.iwindplus.mgt.api.upms.vo.RoleBaseVO;
import com.iwindplus.mgt.api.upms.vo.UserDepartmentInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserDetailVO;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.api.upms.vo.UserInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserOrgInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 用户查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER})
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserOrgRepository userOrgRepository;
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final OssClient ossClient;
    private final MailCaptchaLogClient mailCaptchaLogClient;
    private final SmsCaptchaLogClient smsCaptchaLogClient;
    private final MgtProperty property;
    private final UserExtendYubikeyRepository userExtendYubikeyRepository;

    /**
     * 分页查询用户列表.
     *
     * @param entity 查询条件
     * @return 分页查询结果
     */
    public IPage<UserPageVO> page(UserSearchDTO entity) {
        final PageDTO<UserPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.userRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

    /**
     * 根据条件查询用户列表.
     *
     * @param param 查询条件
     * @return 查询结果
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
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

    /**
     * 根据用户ID列表查询用户列表.
     *
     * @param ids 用户ID列表
     * @return 查询结果
     */
    @Cacheable(value = {"user::listInfoByIds"}, keyGenerator = BeanConstant.BEAN_KEY_GENERATOR, unless = "#result == null")
    public List<UserVO> listInfoByIds(List<Long> ids) {
        CheckDataUtil.checkBatchOperationSize(ids.size(), NumberConstant.NUMBER_FIFTY);
        final List<UserDO> list = this.userRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyToList(list, UserVO.class);
    }

    /**
     * 根据用户ID列表查询用户扩展列表.
     *
     * @param ids 用户ID列表
     * @return 查询结果
     */
    public List<UserExtendVO> listExtendByIds(List<Long> ids) {
        CheckDataUtil.checkBatchOperationSize(ids.size(), NumberConstant.NUMBER_FIFTY);
        final List<UserVO> list = this.listInfoByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<UserExtendVO> voList = BeanUtil.copyToList(list, UserExtendVO.class);
        final List<String> relativePaths = new ArrayList<>(10);
        voList.forEach(data -> {
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
        List<FilePathVO> filePaths = OrgApplicationService.getFilePaths(
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        return voList.stream().peek(data -> this.buildImage(filePaths, data, data.getAvatar(), data.getIdCardFront(), data.getIdCardBack())).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 根据角色ID列表查询用户列表.
     *
     * @param roleIds 角色ID列表
     * @return 查询结果
     */
    public List<UserOrgInfoVO> listByRoleIds(List<Long> roleIds) {
        return this.userRepository.getBaseMapper().selectListByRoleIds(roleIds);
    }

    /**
     * 根据部门ID列表查询用户列表.
     *
     * @param departmentIds 部门ID列表
     * @return 查询结果
     */
    public List<UserDepartmentInfoVO> listByDepartmentIds(List<Long> departmentIds) {
        return this.userRepository.getBaseMapper().selectListByDepartmentIds(departmentIds);
    }

    /**
     * 校验用户扩展功能.
     *
     * @param entity 查询条件
     * @return 查询结果
     */
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

    /**
     * 根据条件查询用户.
     *
     * @param entity 查询条件
     * @return 查询结果
     */
    @Cacheable(value = {"user::getByCondition"}, keyGenerator = BeanConstant.BEAN_KEY_GENERATOR, unless = "#result == null")
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
    public UserInfoVO getLoginInfoByCondition(UserBaseQueryDTO entity) {
        UserInfoVO userInfo = this.userRepository.getBaseMapper().selectByCondition(entity);
        if (Objects.isNull(userInfo)) {
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return userInfo;
    }

    /**
     * 根据登录参数查询用户.
     *
     * @param param 登录参数
     * @return 查询结果
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public UserDetailVO getLoginByParam(String param) {
        UserDetailVO data = this.userRepository.getBaseMapper().selectLoginByParam(param);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return this.getCurrentUserInfoVO(data);
    }

    /**
     * 根据登录验证码查询用户.
     *
     * @param code 登录验证码
     * @return 查询结果
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public UserDetailVO getLoginByCode(String code) {
        UserDetailVO data = this.userRepository.getBaseMapper().selectLoginByCode(code);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.CODE_NOT_EXIST);
        }
        return this.getCurrentUserInfoVO(data);
    }

    /**
     * 根据用户ID查询用户详情.
     *
     * @param id 用户ID
     * @return 查询结果
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public UserVO getDetail(Long id) {
        UserDO data = this.userRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, UserVO.class);
    }

    /**
     * 根据用户ID查询用户扩展详情.
     *
     * @param id 用户ID
     * @return 查询结果
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
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
        List<FilePathVO> filePaths = OrgApplicationService.getFilePaths(
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        this.buildImage(filePaths, result, data.getAvatar(), data.getIdCardFront(), data.getIdCardBack());
        return result;
    }

    /**
     * 根据用户ID查询用户扩展详情.
     *
     * @param orgId  租户ID
     * @param userId 用户ID
     * @return 查询结果
     */
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

        List<FilePathVO> filePaths = OrgApplicationService.getFilePaths(
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        if (CollUtil.isEmpty(filePaths)) {
            return data;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getAvatar()) && data.getAvatar().equals(p.getRelativePath())) {
                data.setAvatarStr(p.getAbsolutePath());
            }
        });
        return data;
    }

    /**
     * 根据用户ID查询用户信息.
     *
     * @param orgId  租户ID
     * @param userId 用户ID
     * @return 查询结果
     */
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

    /**
     * 根据相对路径列表查询OSS签名URL列表.
     *
     * @param relativePaths 相对路径列表
     * @param timeout       过期时间
     * @return 查询结果
     */
    public List<FilePathVO> listUserOssSignUrl(List<String> relativePaths, Integer timeout) {
        return Optional.ofNullable(ossClient.listSignUrl(
                property.getOss().getCode(),
                property.getOss().getTplCode(), relativePaths, null))
            .map(ResultVO::getBizData).orElse(null);
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

    private void buildImage(List<FilePathVO> filePaths, UserExtendVO result, String avatar, String idCardFront, String idCardBack) {
        if (CollUtil.isNotEmpty(filePaths)) {
            filePaths.forEach(p -> {
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
}
