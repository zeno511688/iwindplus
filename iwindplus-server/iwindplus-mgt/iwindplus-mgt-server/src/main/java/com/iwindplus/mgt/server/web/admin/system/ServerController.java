/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
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
import com.iwindplus.mgt.domain.dto.system.ServerDTO;
import com.iwindplus.mgt.domain.dto.system.ServerSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ServerBaseVO;
import com.iwindplus.mgt.domain.vo.system.ServerPageVO;
import com.iwindplus.mgt.domain.vo.system.ServerVO;
import com.iwindplus.mgt.server.service.system.ServerService;
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
 * 服务相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "服务接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/server")
@Validated
@RequiredArgsConstructor
public class ServerController extends BaseController {

    private final ServerService serverService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "server", operateType = "save", operateName = "添加", operateDesc = "添加服务")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) ServerDTO entity) {
        boolean data = this.serverService.save(entity);
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
    @RedisIdempotent
    @OperateLog(bizType = "server", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除服务")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.serverService.removeByIds(ids);
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
    @OperateValid(enabledGa = true)
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "server", operateType = "edit", operateName = "编辑", operateDesc = "编辑服务")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) ServerDTO entity) {
        boolean data = this.serverService.edit(entity);
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
    @OperateValid(enabledGa = true)
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "server", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑服务状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.serverService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "server", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑服务设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.serverService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < ServerPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<ServerPageVO>> page(@Validated ServerSearchDTO entity) {
        IPage<ServerPageVO> data = this.serverService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < ServerBaseVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<ServerBaseVO>> listEnabled() {
        List<ServerBaseVO> data = this.serverService.listEnabled();
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<ServerVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<ServerVO> getDetail(@RequestParam Long id) {
        ServerVO data = this.serverService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 刷新服务.
     */
    @Operation(summary = "刷新服务")
    @GetMapping("flush")
    public void flush() {
        this.serverService.flush();
    }
}
