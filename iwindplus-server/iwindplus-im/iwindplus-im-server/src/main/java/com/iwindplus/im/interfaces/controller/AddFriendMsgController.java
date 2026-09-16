/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.application.query.AddFriendMsgQueryService;
import com.iwindplus.im.application.query.dto.AddFriendMsgSearchDTO;
import com.iwindplus.im.application.query.vo.AddFriendMsgPageVO;
import com.iwindplus.im.application.query.vo.AddFriendMsgVO;
import com.iwindplus.im.application.service.AddFriendMsgApplicationService;
import com.iwindplus.im.common.enums.MsgStatusEnum;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加好友消息相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "加好友消息接口")
@Slf4j
@RestController
@RequestMapping("admin/im/add/friend/msg")
@Validated
@RequiredArgsConstructor
public class AddFriendMsgController extends BaseController {

    private final AddFriendMsgQueryService addFriendMsgQueryService;
    private final AddFriendMsgApplicationService addFriendMsgService;

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
        boolean data = this.addFriendMsgService.removeByIds(ids);
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
    @PutMapping("editMsgStatus")
    public ResultVO<Boolean> editMsgStatus(@RequestParam String id, @RequestParam MsgStatusEnum status) {
        boolean data = this.addFriendMsgService.editMsgStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < AddFriendMsgPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<AddFriendMsgPageVO>> page(PageDTO<AddFriendMsgDO> page, @Validated AddFriendMsgSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<AddFriendMsgPageVO> data = this.addFriendMsgQueryService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id      主键
     * @return ResultVO<AddFriendMsgVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<AddFriendMsgVO> getDetail(@RequestParam String id) {
        AddFriendMsgVO data = this.addFriendMsgQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
