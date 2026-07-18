/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.domain.dto.MailTplEditDTO;
import com.iwindplus.setup.domain.dto.MailTplSaveDTO;
import com.iwindplus.setup.domain.dto.MailTplSearchDTO;
import com.iwindplus.setup.domain.vo.MailTplBaseVO;
import com.iwindplus.setup.domain.vo.MailTplPageVO;
import com.iwindplus.setup.domain.vo.MailTplVO;
import com.iwindplus.setup.server.dal.model.MailTplDO;
import com.iwindplus.setup.server.service.MailTplService;
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
 * 邮箱模板配置相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "邮箱模板配置接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/mail/tpl")
@Validated
@RequiredArgsConstructor
public class MailTplController extends BaseController {

    private final MailTplService mailTplService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "mailTpl", operateType = "save", operateName = "添加", operateDesc = "添加邮箱模板配置")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) MailTplSaveDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.mailTplService.save(entity);
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
    @OperateLog(bizType = "mailTpl", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除邮箱模板配置")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.mailTplService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "mailTpl", operateType = "edit", operateName = "编辑", operateDesc = "编辑邮箱模板配置")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) MailTplEditDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.mailTplService.edit(entity);
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
    @OperateLog(keys = "#entity.id", bizType = "mailTpl", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑邮箱模板配置状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.mailTplService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "mailTpl", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑邮箱模板配置设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.mailTplService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return ResultVO < IPage < MailTplPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<MailTplPageVO>> page(PageDTO<MailTplDO> page, @Validated MailTplSearchDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<MailTplPageVO> data = this.mailTplService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < MailTplBaseVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<MailTplBaseVO>> listEnabled() {
        List<MailTplBaseVO> data = this.mailTplService.listEnabled();
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < MailTplVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<MailTplVO> getDetail(@RequestParam Long id) {
        MailTplVO data = this.mailTplService.getDetail(id);
        return ResultVO.success(data);
    }
}
