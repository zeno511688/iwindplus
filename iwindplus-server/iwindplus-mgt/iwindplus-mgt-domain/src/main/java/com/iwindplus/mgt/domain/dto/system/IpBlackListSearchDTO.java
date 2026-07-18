/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;


import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * IP黑名单搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "IP黑名单搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IpBlackListSearchDTO extends DbPageDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * ip.
     */
    @Schema(description = "ip")
    private String ip;
}
