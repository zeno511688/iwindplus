/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.handler;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.handler.EasyExcelImportVerifyHandler;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.domain.dto.WechatConfigMpImportDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.model.WechatConfigMpDO;
import com.iwindplus.setup.server.dal.repository.WechatConfigMpRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 微信公众号配置导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class WechatConfigMpImportVerifyHandler implements EasyExcelImportVerifyHandler<WechatConfigMpImportDTO> {

    private WechatConfigMpRepository wechatConfigMpRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<WechatConfigMpImportDTO> dataList = new ArrayList<>(10);

    public WechatConfigMpImportVerifyHandler(WechatConfigMpRepository wechatConfigMpRepository, UserBaseVO userInfo) {
        this.wechatConfigMpRepository = wechatConfigMpRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(WechatConfigMpImportDTO data) {
        StringBuilder msg = new StringBuilder();
        // 校验名称在表格中是否重复
        final boolean izNameRepeat = CharSequenceUtil.isNotBlank(data.getName()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getName()) && Objects.equals(data.getName().trim(), m.getName().trim()));
        if (Boolean.TRUE.equals(izNameRepeat)) {
            msg.append(SetupCodeEnum.NAME_REPEAT_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getNotifyUrl()) && !Validator.isUrl(data.getNotifyUrl().trim())) {
            msg.append(SetupCodeEnum.NOTIFY_URL_FORMAT_ERROR + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getNotifySuccessUrl()) && !Validator.isUrl(data.getNotifySuccessUrl().trim())) {
            msg.append(SetupCodeEnum.NOTIFY_SUCCESS_URL_FORMAT_ERROR + ";");
        }
        // 校验公众号appId在表格中是否重复
        final boolean izRepeat = CharSequenceUtil.isNotBlank(data.getAccessKey()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getAccessKey()) && Objects.equals(data.getAccessKey().trim(), m.getAccessKey().trim()));
        if (Boolean.TRUE.equals(izRepeat)) {
            msg.append(SetupCodeEnum.MP_APP_ID_REPEAT_IN_TABLE + ";");
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

    private void checkNameExist(WechatConfigMpImportDTO data, StringBuilder msg) {
        boolean result = SqlHelper.retBool(
            this.wechatConfigMpRepository.count(Wrappers.lambdaQuery(WechatConfigMpDO.class)
                .eq(WechatConfigMpDO::getName, data.getName().trim())
                .eq(WechatConfigMpDO::getOrgId, userInfo.getOrgId())));
        if (result) {
            msg.append(MgtCodeEnum.NAME_EXIST + ";");
        }
    }

    private void checkAccessKeyExist(WechatConfigMpImportDTO data, StringBuilder msg) {
        long count = this.wechatConfigMpRepository.count(Wrappers.lambdaQuery(WechatConfigMpDO.class)
            .eq(WechatConfigMpDO::getAccessKey, data.getAccessKey().trim())
            .eq(WechatConfigMpDO::getOrgId, userInfo.getOrgId()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(SetupCodeEnum.WECHAT_CONFIG_MA_ACCESS_KEY_EXIST);
        }
    }
}
