/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.OrgAuditDTO;
import com.iwindplus.mgt.domain.dto.power.OrgEditDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSaveDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSearchDTO;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.OrgExtendVO;
import com.iwindplus.mgt.domain.vo.power.OrgPageVO;
import com.iwindplus.mgt.domain.vo.power.OrgVO;
import java.util.List;

/**
 * 组织业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface OrgService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(OrgSaveDTO entity);

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
    boolean edit(OrgEditDTO entity);

    /**
     * 编辑审核状态（提交审核，审核，驳回）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean editAuditStatus(OrgAuditDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, EnableStatusEnum status);

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    boolean editBuildIn(Long id, Boolean buildInFlag);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<OrgPageVO>
     */
    IPage<OrgPageVO> page(OrgSearchDTO entity);

    /**
     * 获取用户组织列表（标记选中的）.
     *
     * @param userId 用户主键
     * @return List<OrgBaseCheckedVO>
     */
    List<OrgBaseCheckedVO> listByUserId(Long userId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return OrgVO
     */
    OrgVO getDetail(Long id);

    /**
     * 详情.
     *
     * @param id 主键
     * @return OrgExtendVO
     */
    OrgExtendVO getDetailExtend(Long id);

    /**
     * 查询用户组织.
     *
     * @param userId 用户主键
     * @return OrgBaseCheckedVO
     */
    OrgBaseCheckedVO getOrg(Long userId);

    /**
     * 查询用户组织主键.
     *
     * @param userId 用户主键
     * @return Long
     */
    Long getOrgId(Long userId);
}
