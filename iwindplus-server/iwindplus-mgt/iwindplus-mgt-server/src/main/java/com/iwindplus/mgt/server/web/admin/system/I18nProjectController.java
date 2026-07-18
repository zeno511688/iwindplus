/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.BaseVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.system.I18nProjectExtendDTO;
import com.iwindplus.mgt.domain.dto.system.I18nProjectSearchDTO;
import com.iwindplus.mgt.domain.vo.system.I18nProjectExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nProjectPageVO;
import com.iwindplus.mgt.server.service.system.I18nProjectService;
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
 * 国际化项目相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "国际化项目接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/i18nProject")
@Validated
@RequiredArgsConstructor
public class I18nProjectController extends BaseController {

    private final I18nProjectService i18nProjectService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "i18nProject", operateType = "save", operateName = "添加", operateDesc = "添加国际化项目")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) I18nProjectExtendDTO entity) {
        boolean data = this.i18nProjectService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "i18nProject", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除国际化项目")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.i18nProjectService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "i18nProject", operateType = "edit", operateName = "编辑", operateDesc = "编辑国际化项目")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) I18nProjectExtendDTO entity) {
        boolean data = this.i18nProjectService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑状态")
    @PutMapping("editStatus")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "i18nProject", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑国际化项目状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.i18nProjectService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "i18nProject", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑国际化项目设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.i18nProjectService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < I18nProjectPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<I18nProjectPageVO>> page(@Validated I18nProjectSearchDTO entity) {
        IPage<I18nProjectPageVO> data = this.i18nProjectService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<I18nProjectExtendVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<I18nProjectExtendVO> getDetail(@RequestParam Long id) {
        I18nProjectExtendVO data = this.i18nProjectService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < BaseVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<BaseVO>> listEnabled() {
        List<BaseVO> data = this.i18nProjectService.listEnabled();
        return ResultVO.success(data);
    }

    /**
     * 推送数据.
     *
     * @param id 主键
     */
    @Operation(summary = "推送数据")
    @GetMapping("pushData")
    public ResultVO<Boolean> pushData(@RequestParam Long id) {
        boolean data = this.i18nProjectService.pushData(id);
        return ResultVO.success(data);
    }

    /**
     * 下载.
     *
     * @param id 主键
     */
    @Operation(summary = "下载")
    @GetMapping("download")
    public void downloadFile(@RequestParam Long id) {
        this.i18nProjectService.download(id, this.getResponse());
    }
}
