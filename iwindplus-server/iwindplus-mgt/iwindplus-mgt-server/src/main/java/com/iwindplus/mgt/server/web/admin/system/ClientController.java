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
import com.iwindplus.mgt.domain.dto.system.ClientDTO;
import com.iwindplus.mgt.domain.dto.system.ClientSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ClientBaseVO;
import com.iwindplus.mgt.domain.vo.system.ClientPageVO;
import com.iwindplus.mgt.domain.vo.system.ClientVO;
import com.iwindplus.mgt.server.service.system.ClientService;
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
 * 客户端相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "客户端接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/client")
@Validated
@RequiredArgsConstructor
public class ClientController extends BaseController {

    private final ClientService clientService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < ClientBaseVO>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "client", operateType = "save", operateName = "添加", operateDesc = "添加客户端")
    public ResultVO<ClientBaseVO> save(@RequestBody @Validated({SaveGroup.class}) ClientDTO entity) {
        ClientBaseVO data = this.clientService.save(entity);
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
    @RedisIdempotent
    @OperateLog(bizType = "client", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除客户端")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.clientService.removeByIds(ids);
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
    @OperateValid(enabledGa = true)
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "client", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑客户端状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.clientService.editStatus(id, status);
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
    @OperateValid(enabledGa = true)
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "client", operateType = "edit", operateName = "编辑", operateDesc = "编辑客户端")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) ClientDTO entity) {
        boolean data = this.clientService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 重置密钥.
     *
     * @param id 主键
     * @return ResultVO < ClientBaseVO>
     */
    @Operation(summary = "重置密钥")
    @PutMapping("editSecret")
    @OperateValid(enabledGa = true)
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "client", operateType = "editSecret", operateName = "重置密钥", operateDesc = "重置客户端密钥")
    public ResultVO<ClientBaseVO> editSecret(@RequestParam Long id) {
        ClientBaseVO data = this.clientService.editSecret(id);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < ClientPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<ClientPageVO>> page(@Validated ClientSearchDTO entity) {
        IPage<ClientPageVO> data = this.clientService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 通过客户端id查询.
     *
     * @param clientId 客户端id
     * @return ResultVO < ClientVO>
     */
    @Operation(summary = "通过客户端id查询")
    @GetMapping("getByClientId")
    public ResultVO<ClientVO> getByClientId(@RequestParam String clientId) {
        ClientVO data = this.clientService.getByClientId(clientId);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < ClientVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<ClientVO> getDetail(@RequestParam String id) {
        ClientVO data = this.clientService.getDetail(id);
        return ResultVO.success(data);
    }
}
