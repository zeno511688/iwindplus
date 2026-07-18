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
import com.iwindplus.im.domain.dto.DirectMsgSearchDTO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.vo.DirectMsgPageVO;
import com.iwindplus.im.domain.vo.DirectMsgVO;
import com.iwindplus.im.server.dal.model.DirectMsgDO;
import com.iwindplus.im.server.service.DirectMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直发消息相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "直发消息接口")
@Slf4j
@RestController
@RequestMapping("admin/im/direct/msg")
@Validated
public class DirectMsgController extends BaseController {

    @Resource
    private DirectMsgService directMsgService;

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
        boolean data = this.directMsgService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑消息状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑消息状态")
    @PutMapping("editMsgStatus")
    public ResultVO<Boolean> editMsgStatus(@RequestParam String id, @RequestParam MsgStatusEnum status) {
        boolean data = this.directMsgService.editMsgStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < DirectMsgPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<DirectMsgPageVO>> page(PageDTO<DirectMsgDO> page, @Validated DirectMsgSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<DirectMsgPageVO> data = this.directMsgService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return ResultVO<DirectMsgVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<DirectMsgVO> getDetail(@RequestParam String id, @RequestParam(required = false) String ossTplCode) {
        DirectMsgVO data = this.directMsgService.getDetail(id, ossTplCode);
        return ResultVO.success(data);
    }
}
