/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.service.ws.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.auth.client.AuthorizationClient;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.web.support.WebManager;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import com.iwindplus.im.domain.enums.SubMsgTypeEnum;
import com.iwindplus.im.server.config.property.ImProperty;
import com.iwindplus.im.server.service.ChatGroupService;
import com.iwindplus.im.server.service.ws.WsMsgService;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HeaderName;
import org.tio.http.common.HeaderValue;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

/**
 * websocket消息处理器.
 *
 * @author zengdegui
 * @since 2023/11/06 21:51
 */
@Slf4j
@Component
public class WsMsgHandler implements IWsMsgHandler {

    @Resource
    private ImProperty property;

    @Resource
    private AuthorizationClient authorizationClient;

    @Resource
    private ChatGroupService chatGroupService;

    @Resource
    private WsMsgService wsMsgService;

    @Resource
    private WebManager webManager;

    @Override
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        final String clientIp = httpRequest.getClientIp();
        final String token = httpRequest.getHeader(HeaderConstant.SEC_WEBSOCKET_PROTOCOL.toLowerCase());
        if (ObjectUtil.isEmpty(token)) {
            log.warn("handshake，token为空，无法连接，ip={}", clientIp);

            final ResultVO<Object> data = ResultVO.error(BizCodeEnum.TOKEN_NOT_EXIST);
            httpResponse.setBody(JacksonUtil.toJsonBytes(data));
            return httpResponse;
        }

        // 从请求头获取用户信息
        UserBaseVO userInfo;
        final String userInfoStr = httpRequest.getHeader(HeaderConstant.X_USER_INFO.toLowerCase());
        if (ObjectUtil.isEmpty(userInfoStr)) {
            // 为空则从token解析
            userInfo = HttpsUtil.getUserInfo(token);
        } else {
            userInfo = this.webManager.getUserInfo(userInfoStr);
        }

        if (ObjectUtil.isEmpty(userInfo)) {
            final ResultVO<Object> data = ResultVO.error(BizCodeEnum.INVALID_ACCESS_TOKEN);
            httpResponse.setBody(JacksonUtil.toJsonBytes(data));
            return httpResponse;
        }

        // 设置通道信息
        channelContext.setUserid(userInfo.getUserId().toString());
        channelContext.setToken(token);
        channelContext.setAttribute(HeaderConstant.X_USER_INFO, userInfo);
        // 删除用户旧通道信息
        Tio.closeUser(channelContext.tioConfig, userInfo.getUserId().toString(), null);
        // 绑定用户，用于发送用户消息
        Tio.bindUser(channelContext, userInfo.getUserId().toString());
        // 获取用户的群
        final List<Long> groupList = this.chatGroupService.listByUserId(userInfo.getUserId(), userInfo.getOrgId());
        if (CollUtil.isNotEmpty(groupList)) {
            groupList.forEach(group -> Tio.bindGroup(channelContext, group.toString()));
        }

        // 如果传了请求头参数sec-websocket-protocol，需要响应客户端返回回去
        httpResponse.addHeader(HeaderName.from(HeaderConstant.SEC_WEBSOCKET_PROTOCOL.toLowerCase()), HeaderValue.from(token));
        return httpResponse;
    }

    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        log.info("握手成功之后执行");
    }

    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        log.info("接收到字节消息");
        try {
            UserBaseVO userInfo = this.getUserInfo(channelContext);
            if (ObjectUtil.isEmpty(userInfo)) {
                this.sendErrorMsg(channelContext);
                return null;
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        } finally {
            UserContextHolder.remove();
        }
        return null;
    }

    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        log.info("关闭连接");
        try {
            Tio.remove(channelContext, "删除连接");
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        } finally {
            UserContextHolder.remove();
        }
        return null;
    }

    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
        log.info("接收到文本消息={}", text);

        WsSendMsgDTO entity = JacksonUtil.parseObject(text, WsSendMsgDTO.class);
        if (Objects.isNull(entity)) {
            log.warn("数据格式不正确={}", text);
            return null;
        }
        try {
            UserBaseVO userInfo = this.getUserInfo(channelContext);
            if (ObjectUtil.isEmpty(userInfo)) {
                this.sendErrorMsg(channelContext);
                return null;
            }
            entity.setSendUserId(userInfo.getUserId());
            entity.setSendOrgId(userInfo.getOrgId());
            this.wsMsgService.sendWsMsg(entity, channelContext);
        } catch (Exception ex) {
            log.warn(ExceptionConstant.EXCEPTION, ex);
        } finally {
            UserContextHolder.remove();
        }
        return null;
    }

    private UserBaseVO getUserInfo(ChannelContext channelContext) {
        UserBaseVO userInfo;
        final String token = channelContext.getToken();
        if (CharSequenceUtil.isBlank(token)) {
            final Object attribute = channelContext.getAttribute(HeaderConstant.X_USER_INFO);
            if (!(attribute instanceof UserBaseVO data)) {
                return null;
            } else {
                return data;
            }
        } else {
            if (this.property.getEnabledRemoteToken()) {
                final ResultVO<UserBaseVO> response = this.authorizationClient.checkAccessToken(token);
                response.errorThrow();
                userInfo = response.getBizData();
            } else {
                userInfo = HttpsUtil.getUserInfo(token);
            }
        }

        if (Objects.nonNull(userInfo)) {
            UserContextHolder.setContext(userInfo);
        }

        return userInfo;
    }

    private void sendErrorMsg(ChannelContext channelContext) {
        final String userId = channelContext.userid;
        if (ObjectUtil.isEmpty(userId)) {
            return;
        }

        final String message = ResultVO.message(BizCodeEnum.INVALID_ACCESS_TOKEN.getBizCode(), null,
            BizCodeEnum.INVALID_ACCESS_TOKEN.getBizMessage(), null);
        final WsSendMsgDTO entity = WsSendMsgDTO.builder()
            .command(CommandEnum.PERSON_NOTICE_MSG)
            .subMsgType(SubMsgTypeEnum.ACCESS_TOKEN_EXPIRED.getValue())
            .msgType(MsgTypeEnum.TEXT)
            .title(message)
            .content(message)
            .receiverId(Long.valueOf(userId))
            .build();
        this.wsMsgService.sendWsMsg(entity, channelContext);
    }
}
