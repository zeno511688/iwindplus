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
import com.iwindplus.setup.domain.dto.SmsConfigImportDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.model.SmsConfigDO;
import com.iwindplus.setup.server.dal.repository.SmsConfigRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 短信配置导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class SmsConfigImportVerifyHandler implements EasyExcelImportVerifyHandler<SmsConfigImportDTO> {

    private SmsConfigRepository smsConfigRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<SmsConfigImportDTO> dataList = new ArrayList<>(10);

    public SmsConfigImportVerifyHandler(SmsConfigRepository smsConfigRepository, UserBaseVO userInfo) {
        this.smsConfigRepository = smsConfigRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(SmsConfigImportDTO data) {
        StringBuilder msg = new StringBuilder();
        // 校验名称在表格中是否重复
        final boolean izNameRepeat = CharSequenceUtil.isNotBlank(data.getName()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getName()) && Objects.equals(data.getName().trim(), m.getName().trim()));
        if (Boolean.TRUE.equals(izNameRepeat)) {
            msg.append(SetupCodeEnum.NAME_REPEAT_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getName())) {
            this.checkNameExist(data, msg);
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

    private void checkNameExist(SmsConfigImportDTO data, StringBuilder msg) {
        boolean result = SqlHelper.retBool(
            this.smsConfigRepository.count(Wrappers.lambdaQuery(SmsConfigDO.class)
                .eq(SmsConfigDO::getName, data.getName().trim())
                .eq(SmsConfigDO::getOrgId, userInfo.getOrgId())));
        if (result) {
            msg.append(MgtCodeEnum.NAME_EXIST + ";");
        }
    }
}
