/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 历史流程任务参与人表.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "历史流程任务参与人实体对象")
@TableName(value = "`flow_his_task_player`")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class FlowHisTaskPlayerDO extends FlowTaskPlayerDO {

}
