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
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.domain.dto.SmsConfigEditDTO;
import com.iwindplus.setup.domain.dto.SmsConfigSaveDTO;
import com.iwindplus.setup.domain.dto.SmsConfigSearchDTO;
import com.iwindplus.setup.domain.vo.SmsConfigBaseVO;
import com.iwindplus.setup.domain.vo.SmsConfigPageVO;
import com.iwindplus.setup.domain.vo.SmsConfigVO;
import com.iwindplus.setup.server.dal.model.SmsConfigDO;
import com.iwindplus.setup.server.service.SmsConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 短信配置相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "短信配置接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/sms/config")
@Validated
@RequiredArgsConstructor
public class SmsConfigController extends BaseController {

    private final SmsConfigService smsConfigService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "smsConfig", operateType = "save", operateName = "添加", operateDesc = "添加短信配置")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) SmsConfigSaveDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.smsConfigService.save(entity);
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
    @OperateLog(bizType = "smsConfig", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除短信配置")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.smsConfigService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "smsConfig", operateType = "edit", operateName = "编辑", operateDesc = "编辑短信配置")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) SmsConfigEditDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.smsConfigService.edit(entity);
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
    @OperateLog(keys = "#entity.id", bizType = "smsConfig", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑短信配置状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.smsConfigService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "smsConfig", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑短信配置设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.smsConfigService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return ResultVO < IPage < SmsConfigPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<SmsConfigPageVO>> page(PageDTO<SmsConfigDO> page, @Validated SmsConfigSearchDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<SmsConfigPageVO> data = this.smsConfigService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < SmsConfigBaseVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<SmsConfigBaseVO>> listEnabled() {
        List<SmsConfigBaseVO> data = this.smsConfigService.listEnabled();
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < SmsConfigVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<SmsConfigVO> getDetail(@RequestParam Long id) {
        SmsConfigVO data = this.smsConfigService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 导出模版.
     *
     * @param response 响应
     */
    @Operation(summary = "导出模版")
    @GetMapping("exportTemplate")
    public void exportTemplate(HttpServletResponse response) {
        this.smsConfigService.exportTemplate(response);
    }

    /**
     * 导入.
     *
     * @param file     文件
     * @param response 响应
     */
    @Operation(summary = "导入")
    @PostMapping("importByTemplate")
    @RedisIdempotent
    public void importByTemplate(@RequestPart MultipartFile file, HttpServletResponse response) {
        UserBaseVO userInfo = this.getUserInfo();
        this.smsConfigService.importByTemplate(file, userInfo, response);
    }
}
