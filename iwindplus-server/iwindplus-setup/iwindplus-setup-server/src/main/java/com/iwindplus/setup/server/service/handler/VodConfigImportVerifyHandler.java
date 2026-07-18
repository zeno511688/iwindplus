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
import com.iwindplus.setup.domain.dto.VodConfigImportDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.model.VodConfigDO;
import com.iwindplus.setup.server.dal.repository.VodConfigRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 对象存储配置导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class VodConfigImportVerifyHandler implements EasyExcelImportVerifyHandler<VodConfigImportDTO> {

    private VodConfigRepository vodConfigRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<VodConfigImportDTO> dataList = new ArrayList<>(10);

    public VodConfigImportVerifyHandler(VodConfigRepository vodConfigRepository, UserBaseVO userInfo) {
        this.vodConfigRepository = vodConfigRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(VodConfigImportDTO data) {
        data.setErrorMsg(null);
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
        // 校验服务器区域在表格中是否重复
        final boolean izRepeat = Objects.nonNull(data.getType()) && CharSequenceUtil.isNotBlank(data.getRegion())
            && this.dataList.stream().filter(Objects::nonNull).anyMatch(m -> Objects.nonNull(m.getType()) && Objects.equals(data.getType(), m.getType())
            && CharSequenceUtil.isNotBlank(m.getRegion()) && Objects.equals(data.getRegion().trim(), m.getRegion().trim()));
        if (Boolean.TRUE.equals(izRepeat)) {
            msg.append(SetupCodeEnum.SERVER_REGION_ID_REPEAT_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getName())) {
            this.checkNameExist(data, msg);
        }
        if (CharSequenceUtil.isNotBlank(data.getRegion())) {
            this.checkRegionExist(data, msg);
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

    private void checkNameExist(VodConfigImportDTO data, StringBuilder msg) {
        boolean result = SqlHelper.retBool(
            this.vodConfigRepository.count(Wrappers.lambdaQuery(VodConfigDO.class)
                .eq(VodConfigDO::getName, data.getName().trim())
                .eq(VodConfigDO::getOrgId, userInfo.getOrgId())));
        if (result) {
            msg.append(MgtCodeEnum.NAME_EXIST + ";");
        }
    }

    private void checkRegionExist(VodConfigImportDTO data, StringBuilder msg) {
        long count = this.vodConfigRepository.count(Wrappers.lambdaQuery(VodConfigDO.class)
            .eq(VodConfigDO::getType, data.getType())
            .eq(VodConfigDO::getRegion, data.getRegion().trim())
            .eq(VodConfigDO::getOrgId, userInfo.getOrgId()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(SetupCodeEnum.SERVER_REGION_EXIST);
        }
    }
}
