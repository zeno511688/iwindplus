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
import com.iwindplus.setup.domain.dto.WechatConfigMaEditDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMaSaveDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMaSearchDTO;
import com.iwindplus.setup.domain.vo.WechatConfigMaPageVO;
import com.iwindplus.setup.domain.vo.WechatConfigMaVO;
import com.iwindplus.setup.server.dal.model.WechatConfigMaDO;
import com.iwindplus.setup.server.service.WechatConfigMaService;
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
 * 微信小程序配置相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "微信小程序配置接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/wechat/config/ma")
@Validated
@RequiredArgsConstructor
public class WechatConfigMaController extends BaseController {

    private final WechatConfigMaService wechatConfigMaService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "wechatConfigMa", operateType = "save", operateName = "添加", operateDesc = "添加微信小程序配置")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) WechatConfigMaSaveDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.wechatConfigMaService.save(entity);
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
    @OperateLog(bizType = "wechatConfigMa", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除微信小程序配置")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.wechatConfigMaService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "wechatConfigMa", operateType = "edit", operateName = "编辑", operateDesc = "编辑微信小程序配置")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) WechatConfigMaEditDTO entity) {
        boolean data = this.wechatConfigMaService.edit(entity);
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
    @OperateLog(keys = "#entity.id", bizType = "wechatConfigMa", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑微信小程序配置状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.wechatConfigMaService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "wechatConfigMa", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑微信小程序配置设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.wechatConfigMaService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return ResultVO < IPage < WechatConfigMaPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<WechatConfigMaPageVO>> page(PageDTO<WechatConfigMaDO> page, @Validated WechatConfigMaSearchDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<WechatConfigMaPageVO> data = this.wechatConfigMaService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode oss模板编码
     * @return ResultVO < WechatConfigMaVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<WechatConfigMaVO> getDetail(
        @RequestParam Long id,
        @RequestParam(required = false) String ossTplCode) {
        WechatConfigMaVO data = this.wechatConfigMaService.getDetail(id, ossTplCode);
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
        this.wechatConfigMaService.exportTemplate(response);
    }

    /**
     * 导入.
     *
     * @param file     文件
     * @param response 响应
     */
    @RedisIdempotent
    @Operation(summary = "导入")
    @PostMapping("importByTemplate")
    public void importByTemplate(@RequestPart MultipartFile file, HttpServletResponse response) {
        UserBaseVO userInfo = this.getUserInfo();
        this.wechatConfigMaService.importByTemplate(file, userInfo, response);
    }
}
