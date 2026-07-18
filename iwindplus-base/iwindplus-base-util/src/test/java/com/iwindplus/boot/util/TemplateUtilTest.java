/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.boot.util;

import cn.hutool.core.lang.Assert;
import com.iwindplus.base.util.TemplateUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 模板变量替换工具类测试.
 *
 * @author zengdegui
 * @since 2024/03/17 22:33
 */
public class TemplateUtilTest {
    @Test
    public void test1() {
        Assert.isTrue(true);
        String templateContent = "验证码： ${captcha}，${timeout}分钟内有效，误泄露给他人，如非本人操作请忽略。";
        List<String> params = new ArrayList<>(10);
        params.add("123456");
        params.add("10");
        final String templateContent2 = TemplateUtil.getTemplateContent(templateContent, params);
        System.out.println(templateContent2);
    }
}
