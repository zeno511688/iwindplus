/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.upms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.query.upms.organization.PositionQueryService;
import com.iwindplus.mgt.application.query.upms.organization.dto.PositionSearchDTO;
import com.iwindplus.mgt.application.query.upms.organization.vo.PositionBaseCheckedVO;
import com.iwindplus.mgt.application.query.upms.organization.vo.PositionExtendVO;
import com.iwindplus.mgt.application.query.upms.organization.vo.PositionPageVO;
import com.iwindplus.mgt.application.service.upms.organization.PositionApplicationService;
import com.iwindplus.mgt.application.service.upms.organization.dto.PositionEditDTO;
import com.iwindplus.mgt.application.service.upms.organization.dto.PositionSaveDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
 * 职位相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "职位接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/position")
@Validated
@RequiredArgsConstructor
public class PositionController extends BaseController {

    private final PositionApplicationService positionApplicationService;
    private final PositionQueryService positionQueryService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "position", operateType = "save", operateName = "添加", operateDesc = "添加职位")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) PositionSaveDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.positionApplicationService.save(entity);
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
    @OperateLog(bizType = "position", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除职位")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.positionApplicationService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "position", operateType = "edit", operateName = "编辑", operateDesc = "编辑职位")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) PositionEditDTO entity) {
        boolean data = this.positionApplicationService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "position", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑职位状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.positionApplicationService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "position", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑职位设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.positionApplicationService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < PositionPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<PositionPageVO>> page(@Validated PositionSearchDTO entity) {
        IPage<PositionPageVO> data = this.positionQueryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 用户所属职位列表.
     *
     * @param userId        用户主键
     * @param departmentIds 部门主键集合
     * @return ResultVO < List < PositionBaseCheckedVO>>
     */
    @Operation(summary = "用户所属职位列表")
    @GetMapping("listByUserId")
    public ResultVO<List<PositionBaseCheckedVO>> listByUserId(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) List<Long> departmentIds) {
        Long orgId = this.getUserInfo().getOrgId();
        List<PositionBaseCheckedVO> data = this.positionQueryService.listByUserId(orgId, userId, departmentIds);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO < PositionExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<PositionExtendVO> getDetailExtend(@RequestParam Long id) {
        PositionExtendVO data = this.positionQueryService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
