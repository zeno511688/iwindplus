/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.handler.EasyExcelImportVerifyHandler;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.domain.dto.WechatConfigMaImportDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.model.WechatConfigMaDO;
import com.iwindplus.setup.server.dal.repository.WechatConfigMaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 微信小程序配置导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class WechatConfigMaImportVerifyHandler implements EasyExcelImportVerifyHandler<WechatConfigMaImportDTO> {

    private WechatConfigMaRepository wechatConfigMaRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<WechatConfigMaImportDTO> dataList = new ArrayList<>(10);

    public WechatConfigMaImportVerifyHandler(WechatConfigMaRepository wechatConfigMaRepository, UserBaseVO userInfo) {
        this.wechatConfigMaRepository = wechatConfigMaRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(WechatConfigMaImportDTO data) {
        StringBuilder msg = new StringBuilder();
        // 校验名称在表格中是否重复
        final boolean izNameRepeat = CharSequenceUtil.isNotBlank(data.getName()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getName()) && Objects.equals(data.getName().trim(), m.getName().trim()));
        if (Boolean.TRUE.equals(izNameRepeat)) {
            msg.append(SetupCodeEnum.NAME_REPEAT_IN_TABLE + ";");
        }
        // 校验小程序appId在表格中是否重复
        final boolean izRepeat = CharSequenceUtil.isNotBlank(data.getAccessKey()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getAccessKey()) && Objects.equals(data.getAccessKey().trim(), m.getAccessKey().trim()));
        if (Boolean.TRUE.equals(izRepeat)) {
            msg.append(SetupCodeEnum.MA_APP_ID_REPEAT_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getName())) {
            this.checkNameExist(data, msg);
        }
        if (CharSequenceUtil.isNotBlank(data.getAccessKey())) {
            this.checkAccessKeyExist(data, msg);
        }
        this.dataList.add(data);
        ExcelVerifyResultVO result = ExcelVerifyResultVO.builder()
            .success(Boolean.TRUE)
            .build();
        if (CharSequenceUtil.isNotBlank(msg.toString())) {
            result.setSuccess(false);
            result.setMsg(msg.toString());
        }
        return result;
    }

    private void checkNameExist(WechatConfigMaImportDTO data, StringBuilder msg) {
        boolean result = SqlHelper.retBool(
            this.wechatConfigMaRepository.count(Wrappers.lambdaQuery(WechatConfigMaDO.class)
                .eq(WechatConfigMaDO::getName, data.getName().trim())
                .eq(WechatConfigMaDO::getOrgId, userInfo.getOrgId())));
        if (result) {
            msg.append(MgtCodeEnum.NAME_EXIST + ";");
        }
    }

    private void checkAccessKeyExist(WechatConfigMaImportDTO data, StringBuilder msg) {
        long count = this.wechatConfigMaRepository.count(Wrappers.lambdaQuery(WechatConfigMaDO.class)
            .eq(WechatConfigMaDO::getAccessKey, data.getAccessKey().trim())
            .eq(WechatConfigMaDO::getOrgId, userInfo.getOrgId()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(SetupCodeEnum.WECHAT_CONFIG_MA_ACCESS_KEY_EXIST);
        }
    }
}
