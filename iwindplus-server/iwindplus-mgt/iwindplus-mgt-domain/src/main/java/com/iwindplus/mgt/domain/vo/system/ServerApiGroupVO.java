/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务API分组视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务API分组视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerApiGroupVO implements Serializable {

    /**
     * 控制器名称.
     */
    @Schema(description = "控制器名称")
    private String controllerName;

    /**
     * API集合.
     */
    @Schema(description = "API集合")
    private List<ApiVO> apis;

    /**
     * API信息.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiVO implements Serializable {

        /**
         * 主键.
         */
        @Schema(description = "主键")
        private Long id;

        /**
         * 请求方式.
         */
        @Schema(description = "请求方式")
        private String requestMethod;

        /**
         * API名称.
         */
        @Schema(description = "API名称")
        private String apiName;

        /**
         * API路径.
         */
        @Schema(description = "API路径")
        private String apiUrl;
    }

}
