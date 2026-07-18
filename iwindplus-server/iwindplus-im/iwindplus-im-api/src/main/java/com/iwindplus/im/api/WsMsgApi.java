/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.api;

import com.iwindplus.im.domain.dto.WsMsgDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息推送相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface WsMsgApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/ws/";

    /**
     * 发送个人消息通知.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送个人消息通知")
    @PostMapping(API_PREFIX + "sendPersonNoticeMsg")
    void sendPersonNoticeMsg(@RequestBody @Validated WsMsgDTO entity);

    /**
     * 发送系统消息通知.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送系统消息通知")
    @PostMapping(API_PREFIX + "sendSystemNoticeMsg")
    void sendSystemNoticeMsg(@RequestBody @Validated WsMsgDTO entity);

}
