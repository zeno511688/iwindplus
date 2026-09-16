/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.application.query.ChatGroupQueryService;
import com.iwindplus.im.application.service.ChatGroupApplicationService;
import com.iwindplus.im.application.service.dto.ChatGroupEditDTO;
import com.iwindplus.im.application.service.dto.ChatGroupJoinDTO;
import com.iwindplus.im.application.service.dto.ChatGroupSaveDTO;
import com.iwindplus.im.application.query.dto.ChatGroupSearchDTO;
import com.iwindplus.im.application.query.vo.ChatGroupBaseVO;
import com.iwindplus.im.application.query.vo.ChatGroupPageVO;
import com.iwindplus.im.application.query.vo.ChatGroupVO;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
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
 * 聊天群相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "聊天群接口")
@Slf4j
@RestController
@RequestMapping("admin/im/chat/group")
@Validated
@RequiredArgsConstructor
public class ChatGroupController extends BaseController {

    private final ChatGroupQueryService chatGroupQueryService;
    private final ChatGroupApplicationService chatGroupApplicationService;

    /**
     * 添加聊天群.
     *
     * @param entity 对象
     * @return ResultVO<ChatGroupVO>
     */
    @Operation(summary = "添加")
    @PostMapping("saveChatGroup")
    @OperateLog(keys = "#entity.id", bizType = "chatGroup", operateType = "save", operateName = "添加", operateDesc = "添加聊天群")
    public ResultVO<ChatGroupVO> saveChatGroup(@RequestBody @Validated(SaveGroup.class) ChatGroupSaveDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        ChatGroupVO data = this.chatGroupApplicationService.saveChatGroup(entity);
        return ResultVO.success(data);
    }

    /**
     * 加入.
     *
     * @param entity 对象
     * @return ResponseEntity<Boolean>
     */
    @Operation(summary = "加入")
    @PostMapping("saveJoinChatGroup")
    @OperateLog(keys = "#entity.chatGroupId", bizType = "chatGroup", operateType = "saveJoinChatGroup", operateName = "加入", operateDesc = "加入聊天群")
    public ResultVO<Boolean> saveJoinChatGroup(@RequestBody @Validated ChatGroupJoinDTO entity) {
        Boolean data = this.chatGroupApplicationService.saveJoinChatGroup(entity);
        return ResultVO.success(data);
    }

    /**
     * 解散.
     *
     * @param id 主键
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "解散")
    @DeleteMapping("removeChatGroup")
    public ResultVO<Boolean> removeChatGroup(@RequestParam Long id) {
        Long userId = this.getUserInfo().getUserId();
        boolean data = this.chatGroupApplicationService.removeChatGroup(id, userId);
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
    @OperateLog(keys = "#entity.id", bizType = "chatGroup", operateType = "edit", operateName = "编辑", operateDesc = "编辑聊天群")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) ChatGroupEditDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.chatGroupApplicationService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 我的聊天群.
     *
     * @param entity 对象
     * @return ResultVO<IPage < ChatGroupPageVO>>
     */
    @Operation(summary = "我的聊天群")
    @GetMapping("page")
    public ResultVO<IPage<ChatGroupPageVO>> page(PageDTO<ChatGroupDO> page, @Validated ChatGroupSearchDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<ChatGroupPageVO> data = this.chatGroupQueryService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 所属组织的群组.
     *
     * @param orgId 组织主键
     * @return ResultVO<List < ChatGroupBaseVO>>
     */
    @Operation(summary = "所属组织的群组")
    @GetMapping("listByOrgId")
    public ResultVO<List<ChatGroupBaseVO>> listByOrgId(@RequestParam(required = false) Long orgId) {
        orgId = Objects.isNull(orgId) ? this.getUserInfo().getOrgId() : orgId;
        List<ChatGroupBaseVO> data = this.chatGroupQueryService.listByOrgId(orgId);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @return ResultVO<ChatGroupVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<ChatGroupVO> getDetail(@RequestParam Long id) {
        ChatGroupVO data = this.chatGroupQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
