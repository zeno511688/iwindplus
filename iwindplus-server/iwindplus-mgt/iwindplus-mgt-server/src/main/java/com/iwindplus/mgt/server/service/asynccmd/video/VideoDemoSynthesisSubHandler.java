/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd.video;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频合成示例：合成.
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDemoSynthesisSubHandler implements AsyncCmdSubTaskHandler {

    @Override
    public AsyncCmdExecuteResultEnum executeSub(AsyncCmdSubVO entity) {
        return AsyncCmdExecuteResultEnum.EXECUTE;
    }
}
