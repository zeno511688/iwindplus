/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.base.util.domain.vo.GoogleAuthVO;
import com.iwindplus.mgt.domain.dto.power.EditMailDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordByMailDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordByMobileDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSaveUserDTO;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.dto.power.UserSaveByThirdDTO;
import com.iwindplus.mgt.domain.dto.power.UserSaveEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserSearchDTO;
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
import java.util.List;

/**
 * 用户业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface UserService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserSaveEditDTO entity);

    /**
     * 给组织添加用户.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean saveOrgUser(OrgSaveUserDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑（id必选）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(UserSaveEditDTO entity);

    /**
     * 第三方绑定授权注册.
     *
     * @param entity 对象
     * @return UserBindResultVO
     */
    UserBindResultVO editBindByMobile(UserSaveByThirdDTO entity);

    /**
     * 编辑账号状态.
     *
     * @param id      主键
     * @param enabled 账号状态
     * @return boolean
     */
    boolean editEnabled(Long id, Boolean enabled);

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    boolean editBuildIn(Long id, Boolean buildInFlag);

    /**
     * 编辑密码.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean editPassword(EditPasswordDTO entity);

    /**
     * 忘记密码（通过手机号找回）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean editPasswordByMobile(EditPasswordByMobileDTO entity);

    /**
     * 忘记密码（通过邮箱找回）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean editPasswordByMail(EditPasswordByMailDTO entity);

    /**
     * 绑定（更换）邮箱.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean editMail(EditMailDTO entity);

    /**
     * 退出组织.
     *
     * @param userId 用户主键
     * @return boolean
     */
    boolean editExitOrg(Long userId);

    /**
     * 切换组织.
     *
     * @param userOrgId 用户组织关系主键
     * @param userId    用户主键
     * @return boolean
     */
    boolean editChangeOrg(Long userOrgId, Long userId);

    /**
     * 编辑GA绑定状态.
     *
     * @param userId  用户主键
     * @param captcha 验证码
     * @return boolean
     */
    boolean editGaBindFlag(Long userId, String captcha);

    /**
     * 编辑重置GA.
     *
     * @param userId 用户主键
     * @return boolean
     */
    boolean editResetGa(Long userId);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<UserPageVO>
     */
    IPage<UserPageVO> page(UserSearchDTO entity);

    /**
     * 通过条件模糊搜索.
     *
     * @param param 参数
     * @return List<UserVO>
     */
    List<UserVO> listByCondition(String param);

    /**
     * 批量查询
     *
     * @param ids 主键集合
     * @return List<UserVO>
     */
    List<UserVO> listInfoByIds(List<Long> ids);

    /**
     * 批量查询
     *
     * @param ids 主键集合
     * @return List<UserExtendVO>
     */
    List<UserExtendVO> listExtendByIds(List<Long> ids);

    /**
     * 通过角色主键集合查询用户组织信息.
     *
     * @param roleIds 角色主键集合
     * @return List<UserOrgInfoVO>
     */
    List<UserOrgInfoVO> listByRoleIds(List<Long> roleIds);

    /**
     * 通过部门主键集合查询用户部门信息.
     *
     * @param departmentIds 部门主键集合
     * @return List<UserDepartmentInfoVO>
     */
    List<UserDepartmentInfoVO> listByDepartmentIds(List<Long> departmentIds);

    /**
     * 校验用户验证码是否正确.
     *
     * @param entity 对象
     * @return UserExtendFunctionValidVO
     */
    UserExtendFunctionValidVO checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity);

    /**
     * 通过条件查询.
     *
     * @param entity 对象
     * @return UserVO
     */
    UserVO getByCondition(UserBaseQueryDTO entity);

    /**
     * 通过条件查询登录信息.
     *
     * @param entity 对象
     * @return UserInfoVO
     */
    UserInfoVO getLoginInfoByCondition(UserBaseQueryDTO entity);

    /**
     * 用户登录（支持用户名/手机/邮箱/身份证）.
     *
     * @param param 参数
     * @return UserDetailVO
     */
    UserDetailVO getLoginByParam(String param);

    /**
     * 用户登录（支持唯一编码，用于第三方绑定授权方式，如微信公众号，小程序等）.
     *
     * @param code 编码
     * @return UserDetailVO
     */
    UserDetailVO getLoginByCode(String code);

    /**
     * 详情.
     *
     * @param id 主键
     * @return UseVO
     */
    UserVO getDetail(Long id);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return UserExtendVO
     */
    UserExtendVO getDetailExtend(Long id);

    /**
     * 获取登录用户信息.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return UserLoginVO
     */
    UserLoginVO getUserInfo(Long orgId, Long userId);

    /**
     * 获取登录用户扩展信息.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return UserLoginExtendVO
     */
    UserLoginExtendVO getUserExtendInfo(Long orgId, Long userId);

    /**
     * 获取GA二维码.
     *
     * @param userId 用户主键
     * @param width  宽度
     * @param height 高度
     * @return GoogleAuthVO
     */
    GoogleAuthVO getGaQrcode(Long userId, Integer width, Integer height);
}