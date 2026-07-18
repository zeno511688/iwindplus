/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码返回值枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
public enum FlowCodeEnum implements CommonException {
    /**
     * 名称已经存在.
     */
    NAME_EXIST("name_exist", "名称已经存在"),

    /**
     * 编码已经存在.
     */
    CODE_EXIST("code_exist", "编码已经存在"),

    /**
     * 已发布流程不允许修改.
     */
    PUBLISHED_NOT_EDIT("published_not_edit", "已发布流程不允许修改"),

    /**
     * 已停用才能删除.
     */
    DISABLED_CAN_DELETE("disabled_can_delete", "已停用才能删除"),

    /**
     * 业务编号已经存在.
     */
    BIZ_NUMBER_EXIST("biz_number_exist", "业务编号已经存在"),

    /**
     * 流程分类不存在.
     */
    FLOW_CATEGORY_NOT_EXIST("FLOW_CATEGORY_NOT_EXIST", "流程分类不存在"),

    /**
     * 流程表单不存在.
     */
    FLOW_FORM_NOT_EXIST("FLOW_FORM_NOT_EXIST", "流程表单不存在"),

    /**
     * 流程实例不存在.
     */
    FLOW_INSTANCE_NOT_EXIST("flow_instance_not_exist", "流程实例不存在"),

    /**
     * 流程模型不存在.
     */
    FLOW_MODEL_NOT_EXIST("flow_model_not_exist", "流程模型不存在"),

    /**
     * 流程任务不存在.
     */
    FLOW_TASK_NOT_EXIST("flow_task_not_exist", "流程任务不存在"),

    /**
     * 未配置任务的处理人.
     */
    FLOW_TASK_NOT_PLAYER("flow_task_not_player", "未配置任务的处理人"),

    /**
     * 您不是该任务的处理人.
     */
    FLOW_NOT_PLAYER("flow_not_player", "您不是该任务的处理人"),

    /**
     * 流程节点处理人为空.
     */
    FLOW_NODE_PLAYER_EMPTY("flow_node_player_empty", "流程节点处理人为空"),

    /**
     * 只有发起人才能撤销.
     */
    FLOW_ONLY_INITIATOR_REVOKE("flow_only_initiator_revoke", "只有发起人才能撤销"),

    /**
     * 流程节点未找到.
     */
    FLOW_NODE_NOT_FOUND("flow_node_not_found", "流程节点未找到"),

    /**
     * 流程模型未发布.
     */
    FLOW_MODEL_NOT_PUBLISHED("flow_model_not_published", "流程模型未发布"),

    /**
     * 条件节点无匹配分支.
     */
    FLOW_CONDITION_NO_MATCH("flow_condition_no_match", "条件节点无匹配分支"),

    /**
     * 至少保留一个任务处理人.
     */
    FLOW_KEEP_AT_LEAST_ONE_PLAYER("flow_keep_at_least_one_player", "至少保留一个任务处理人"),

    /**
     * 目标节点不存在.
     */
    FLOW_TARGET_NODE_NOT_FOUND("flow_target_node_not_found", "目标节点不存在"),

    /**
     * 不能委托给自己.
     */
    FLOW_DELEGATE_TO_SELF("flow_delegate_to_self", "不能委托给自己"),

    /**
     * 驳回节点无效.
     */
    FLOW_REJECT_NODE_INVALID("flow_reject_node_invalid", "驳回节点无效"),

    /**
     * 跳转节点无效.
     */
    FLOW_JUMP_NODE_INVALID("flow_jump_node_invalid", " 跳转节点无效");

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    FlowCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
