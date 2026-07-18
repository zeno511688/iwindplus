/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.domain.dto.SysNoticeMsgSearchDTO;
import com.iwindplus.im.domain.vo.SysNoticeMsgPageVO;
import com.iwindplus.im.domain.vo.SysNoticeMsgVO;
import com.iwindplus.im.server.dal.model.SysNoticeMsgDO;
import com.iwindplus.im.server.service.SysNoticeMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统通知消息相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "系统通知消息接口")
@Slf4j
@RestController
@RequestMapping("admin/im/sys/notice/msg")
@Validated
public class SysNoticeMsgController extends BaseController {

    @Resource
    private SysNoticeMsgService sysNoticeMsgService;

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    public ResultVO<Boolean> removeByIds(@RequestParam List<String> ids) {
        boolean data = this.sysNoticeMsgService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < SysNoticeMsgPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<SysNoticeMsgPageVO>> page(PageDTO<SysNoticeMsgDO> page, @Validated SysNoticeMsgSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<SysNoticeMsgPageVO> data = this.sysNoticeMsgService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return ResultVO<SysNoticeMsgDO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<SysNoticeMsgVO> getDetail(@RequestParam String id, @RequestParam(required = false) String ossTplCode) {
        SysNoticeMsgVO data = this.sysNoticeMsgService.getDetail(id, ossTplCode);
        return ResultVO.success(data);
    }
}
