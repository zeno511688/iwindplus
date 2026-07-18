/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 邮件发送结果视图对象.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Schema(description = "邮件发送结果视图对象")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MailVO implements Serializable {

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 结果.
     */
    @Schema(description = "结果")
    private Boolean result;

    /**
     * 发送次数.
     */
    @Schema(description = "发送次数")
    private Integer sendCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 创建成功结果视图对象.
     *
     * @param bizNumber 业务流水号
     * @param sendCount 发送次数
     * @return MailVO
     */
    public static MailVO ok(String bizNumber, Integer sendCount) {
        return new MailVO(bizNumber, true, sendCount, null);
    }

    /**
     * 创建失败结果视图对象.
     *
     * @param bizNumber 业务流水号
     * @param sendCount 发送次数
     * @param errorMsg  错误信息
     * @return MailVO
     */
    public static MailVO fail(String bizNumber, Integer sendCount, String errorMsg) {
        return new MailVO(bizNumber, false, sendCount, errorMsg);
    }
}
