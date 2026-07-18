/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.handler.EasyExcelImportVerifyHandler;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.domain.dto.MailConfigImportDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.model.MailConfigDO;
import com.iwindplus.setup.server.dal.repository.MailConfigRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 邮箱配置导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class MailConfigImportVerifyHandler implements EasyExcelImportVerifyHandler<MailConfigImportDTO> {

    private MailConfigRepository mailConfigRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<MailConfigImportDTO> dataList = new ArrayList<>(10);

    public MailConfigImportVerifyHandler(MailConfigRepository mailConfigRepository, UserBaseVO userInfo) {
        this.mailConfigRepository = mailConfigRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(MailConfigImportDTO data) {
        StringBuilder msg = new StringBuilder();
        // 校验名称在表格中是否重复
        final boolean izNameRepeat = CharSequenceUtil.isNotBlank(data.getName()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getName()) && Objects.equals(data.getName().trim(), m.getName().trim()));
        if (Boolean.TRUE.equals(izNameRepeat)) {
            msg.append(SetupCodeEnum.NAME_REPEAT_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getPort()) && !NumberUtil.isInteger(data.getPort().trim())) {
            msg.append(SetupCodeEnum.SMTP_PORT_NOT_INTEGER + ";");
        }
        // 校验发件服务器账户在表格中是否重复
        final boolean izUsernameRepeat = CharSequenceUtil.isNotBlank(data.getUsername()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getUsername()) && Objects.equals(data.getUsername().trim(), m.getUsername().trim()));
        if (Boolean.TRUE.equals(izUsernameRepeat)) {
            msg.append(SetupCodeEnum.SMTP_USERNAME_REPEAT_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getName())) {
            this.checkNameExist(data, msg);
        }
        if (CharSequenceUtil.isNotBlank(data.getUsername())) {
            this.checkUsernameExist(data, msg);
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

    private void checkNameExist(MailConfigImportDTO data, StringBuilder msg) {
        boolean result = SqlHelper.retBool(
            this.mailConfigRepository.count(Wrappers.lambdaQuery(MailConfigDO.class)
                .eq(MailConfigDO::getName, data.getName().trim())
                .eq(MailConfigDO::getOrgId, userInfo.getOrgId())));
        if (result) {
            msg.append(MgtCodeEnum.NAME_EXIST + ";");
        }
    }

    private void checkUsernameExist(MailConfigImportDTO data, StringBuilder msg) {
        long count = this.mailConfigRepository.count(Wrappers.lambdaQuery(MailConfigDO.class)
            .eq(MailConfigDO::getUsername, data.getUsername().trim())
            .eq(MailConfigDO::getOrgId, userInfo.getOrgId()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(SetupCodeEnum.SMTP_USERNAME_EXIST + ";");
        }
    }


}
