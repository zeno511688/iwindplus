/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.decoder;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.feign.domain.property.FeignProperty;
import com.iwindplus.base.util.JacksonUtil;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Feign 统一异常解码器
 *
 * @author zengdegui
 * @since 2025/09/28
 */
@Slf4j
public class FeignErrorDecoder extends ErrorDecoder.Default {

    private final FeignProperty property;

    public FeignErrorDecoder(FeignProperty property) {
        this.property = property;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        // 1. 先交给默认解码器，保留原始异常信息
        Exception exception = super.decode(methodKey, response);

        // 2. 如果关闭自定义解码，直接返回
        if (!property.getError().getEnabled()) {
            return exception;
        }

        int status = response.status();

        // 3. 重试异常直接透传
        if (exception instanceof BizException || exception instanceof RetryableException) {
            return exception;
        }

        log.error("Feign request error, method={}, status={}，message={}", methodKey, status, exception.getMessage());

        // 4. 503 直接抛 BizException
        if (status == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return new BizException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        // 6. 其它情况解析响应体
        ResultVO<Object> result = parseResponse(response);
        return new BizException(
            result.getBizCode(),
            result.getBizMessage(),
            result.getBizMessageParams());
    }

    private ResultVO<Object> parseResponse(Response response) {
        if (response.body() == null) {
            return ResultVO.error(BizCodeEnum.RPC_ERROR);
        }
        // Feign 自动管理流关闭
        try (Reader reader = response.body().asReader(Charset.defaultCharset())) {
            String bodyStr = IoUtil.read(reader);
            if (ObjectUtil.isEmpty(bodyStr)) {
                return ResultVO.error(BizCodeEnum.RPC_ERROR);
            }
            final ResultVO<Object> result = JacksonUtil.parseObject(bodyStr, new TypeReference<>() {
            });
            return result != null ? result : ResultVO.error(BizCodeEnum.RPC_ERROR);
        } catch (IOException e) {
            log.error("读取Feign响应体失败", e);
            return ResultVO.error(BizCodeEnum.RPC_ERROR);
        }
    }
}