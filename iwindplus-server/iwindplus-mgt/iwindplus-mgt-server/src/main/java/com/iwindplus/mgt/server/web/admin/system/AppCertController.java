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
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.system.AppCertDTO;
import com.iwindplus.mgt.domain.dto.system.AppCertSearchDTO;
import com.iwindplus.mgt.domain.vo.system.AppCertBaseVO;
import com.iwindplus.mgt.domain.vo.system.AppCertPageVO;
import com.iwindplus.mgt.domain.vo.system.AppCertVO;
import com.iwindplus.mgt.server.service.system.AppCertService;
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
 * 应用凭证相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "应用凭证接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/appCert")
@Validated
@RequiredArgsConstructor
public class AppCertController extends BaseController {

    private final AppCertService appCertService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加应用凭证")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "appCert", operateType = "save", operateName = "添加", operateDesc = "添加应用凭证")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) AppCertDTO entity) {
        boolean data = this.appCertService.save(entity);
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
    @OperateLog(bizType = "appCert", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除应用凭证")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.appCertService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "appCert", operateType = "edit", operateName = "编辑", operateDesc = "编辑应用凭证")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) AppCertDTO entity) {
        boolean data = this.appCertService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "appCert", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑应用凭证状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.appCertService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "appCert", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑应用凭证设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.appCertService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 重置密钥.
     *
     * @param id 主键
     * @return ResultVO < AppCertBaseVO>
     */
    @Operation(summary = "重置密钥")
    @PutMapping("editSecret")
    @OperateValid(enabledGa = true)
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "appCert", operateType = "editSecret", operateName = "重置密钥", operateDesc = "重置应用凭证密钥")
    public ResultVO<AppCertBaseVO> editSecret(@RequestParam Long id) {
        AppCertBaseVO data = this.appCertService.editSecret(id);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < AppCertPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<AppCertPageVO>> page(@Validated AppCertSearchDTO entity) {
        IPage<AppCertPageVO> data = this.appCertService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<AppCertVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<AppCertVO> getDetail(@RequestParam Long id) {
        AppCertVO data = this.appCertService.getDetail(id);
        return ResultVO.success(data);
    }
}
