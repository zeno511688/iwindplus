/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OssTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssConfigSearchDTO implements Serializable {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 类型.
     */
    @Schema(description = "类型")
    private OssTypeEnum type;

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
