/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.engine.freemarker.FreemarkerEngine;
import com.iwindplus.base.domain.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板变量替换工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class TemplateUtil {

    private TemplateUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 模板变量替换.
     *
     * @param templateContent 模板内容
     * @param templateParams  模板参数集合
     * @return String
     */
    public static String getTemplateContent(String templateContent, List<String> templateParams) {
        Map<String, String> templateParam = getTemplateParam(templateContent, templateParams);
        if (MapUtil.isEmpty(templateParam)) {
            return templateContent;
        }
        final FreemarkerEngine engine = new FreemarkerEngine();
        final Template result = engine.getTemplate(templateContent);
        return result.render(templateParam);
    }

    /**
     * 获取组装后的模板参数.
     *
     * @param templateContent 模板内容
     * @param templateParams  模板参数集合
     * @return Map<String, String>
     */
    public static Map<String, String> getTemplateParam(String templateContent, List<String> templateParams) {
        if (CollUtil.isEmpty(templateParams)) {
            return Collections.emptyMap();
        }
        String[] templateParam = CharSequenceUtil.subBetweenAll(templateContent, CommonConstant.SymbolConstant.DOLLAR_AND_LEFT_CURLY_BRACKET,
            CommonConstant.SymbolConstant.RIGHT_CURLY_BRACKET);
        if (ArrayUtil.isEmpty(templateParam)) {
            return Collections.emptyMap();
        }
        final List<String> paramList = Arrays.asList(templateParam);
        return paramList.stream().collect(Collectors.toMap(
                key -> key,
                key -> {
                    final int index = paramList.indexOf(key);
                    return index < templateParams.size() ? templateParams.get(index) : "";
                },
                (existing, replacement) -> existing
            )
        );
    }
}
