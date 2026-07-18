/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.bo;

import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令业务数据传输对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令业务数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AsyncCmdBO extends AsyncCmdVO {

}
