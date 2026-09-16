/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 云OSS批量删除文件请求参数.
 *
 * <p>在云OSS基类之上，增加删除特有字段（相对路径集合）。</p>
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OssCloudRemoveDTO extends OssRemoveDTO {

    /**
     * 空间名（必填）.
     */
    private String bucketName;
}
