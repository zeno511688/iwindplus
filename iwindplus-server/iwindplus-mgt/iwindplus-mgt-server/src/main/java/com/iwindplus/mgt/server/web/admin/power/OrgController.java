/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.power.OrgAuditDTO;
import com.iwindplus.mgt.domain.dto.power.OrgEditDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSaveDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSearchDTO;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.OrgExtendVO;
import com.iwindplus.mgt.domain.vo.power.OrgPageVO;
import com.iwindplus.mgt.domain.vo.power.OrgVO;
import com.iwindplus.mgt.server.service.power.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "组织接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/org")
@Validated
@RequiredArgsConstructor
public class OrgController extends BaseController {

    private final OrgService orgService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "org", operateType = "save", operateName = "添加", operateDesc = "添加组织")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) OrgSaveDTO entity) {
        boolean data = this.orgService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "org", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除组织")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.orgService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "org", operateType = "edit", operateName = "编辑", operateDesc = "编辑组织")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) OrgEditDTO entity) {
        boolean data = this.orgService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑状态")
    @PutMapping("editStatus")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "org", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑组织状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.orgService.editStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 编辑审核状态.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑审核状态")
    @PutMapping("editAuditStatus")
    @RedisIdempotent
    @OperateLog(keys = {
        "#entity.orgId"}, bizType = "org", operateType = "editAuditStatus", operateName = "编辑审核状态", operateDesc = "编辑组织审核状态（提交审核，审核，驳回）")
    public ResultVO<Boolean> editAuditStatus(@RequestBody @Validated({SaveGroup.class}) OrgAuditDTO entity) {
        boolean data = this.orgService.editAuditStatus(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑设为内置")
    @PutMapping("editBuildIn")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "org", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑组织设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.orgService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < OrgPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<OrgPageVO>> page(@Validated OrgSearchDTO entity) {
        IPage<OrgPageVO> data = this.orgService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 用户组织列表.
     *
     * @return ResultVO < List < OrgBaseCheckedVO>>
     */
    @Operation(summary = "用户所属组织列表")
    @GetMapping("listByUserId")
    public ResultVO<List<OrgBaseCheckedVO>> listByUserId(@RequestParam(required = false) Long userId) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        List<OrgBaseCheckedVO> data = this.orgService.listByUserId(userId);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < OrgExtendVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<OrgVO> getDetail(Long id) {
        id = Optional.ofNullable(id).orElse(this.getUserInfo().getOrgId());
        OrgVO data = this.orgService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO < OrgExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<OrgExtendVO> getDetailExtend(@RequestParam(required = false) Long id) {
        id = Optional.ofNullable(id).orElse(this.getUserInfo().getOrgId());
        OrgExtendVO data = this.orgService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
