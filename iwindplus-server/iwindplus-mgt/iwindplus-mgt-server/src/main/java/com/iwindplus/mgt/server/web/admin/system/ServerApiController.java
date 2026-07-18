/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.web.admin.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.system.ServerApiDTO;
import com.iwindplus.mgt.domain.dto.system.ServerApiSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ServerApiGroupVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiPageVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiVO;
import com.iwindplus.mgt.server.service.system.ServerApiService;
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
 * 服务API相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "服务API接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/serverApi")
@Validated
@RequiredArgsConstructor
public class ServerApiController extends BaseController {

    private final ServerApiService serverApiService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "serverApi", operateType = "save", operateName = "添加", operateDesc = "添加服务API")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) ServerApiDTO entity) {
        boolean data = this.serverApiService.save(entity);
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
    @OperateLog(bizType = "serverApi", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除服务API")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.serverApiService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "serverApi", operateType = "edit", operateName = "编辑", operateDesc = "编辑服务API")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) ServerApiDTO entity) {
        boolean data = this.serverApiService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑设为隐藏.
     *
     * @param id       主键
     * @param hideFlag 是否隐藏
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑设为隐藏")
    @PutMapping("editHideFlag")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "serverApi", operateType = "editHideFlag", operateName = "编辑设为隐藏", operateDesc = "编辑服务API设为隐藏")
    public ResultVO<Boolean> editHideFlag(@RequestParam Long id, @RequestParam Boolean hideFlag) {
        boolean data = this.serverApiService.editHideFlag(id, hideFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < ServerApiPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<ServerApiPageVO>> page(@Validated ServerApiSearchDTO entity) {
        IPage<ServerApiPageVO> data = this.serverApiService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 获取所有API并分组.
     *
     * @return ResultVO < List < ServerApiGroupVO>>
     */
    @Operation(summary = "获取所有API并分组")
    @GetMapping("listApiGroup")
    public ResultVO<List<ServerApiGroupVO>> listApiGroup() {
        List<ServerApiGroupVO> data = this.serverApiService.listApiGroup();
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<ServerApiVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<ServerApiVO> getDetail(@RequestParam Long id) {
        ServerApiVO data = this.serverApiService.getDetail(id);
        return ResultVO.success(data);
    }

}
