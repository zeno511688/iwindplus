/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.application.service.system.security.handler;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.support.EasyExcelImportVerifyHandler;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListImportDTO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * API白名单导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class ApiWhiteListImportVerifyHandler implements EasyExcelImportVerifyHandler<ApiWhiteListImportDTO> {

    private ApiWhiteListRepository apiWhiteListRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<ApiWhiteListImportDTO> dataList = new ArrayList<>(10);

    public ApiWhiteListImportVerifyHandler(ApiWhiteListRepository apiWhiteListRepository, UserBaseVO userInfo) {
        this.apiWhiteListRepository = apiWhiteListRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(ApiWhiteListImportDTO data) {
        StringBuilder msg = new StringBuilder();
        // 校验名称在表格中是否重复
        final boolean izNameRepeat = CharSequenceUtil.isNotBlank(data.getName()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getName()) && Objects.equals(data.getName().trim(), m.getName().trim()));
        if (Boolean.TRUE.equals(izNameRepeat)) {
            msg.append(MgtCodeEnum.NAME_EXIST_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getApiUrl()) && !Validator.isUrl(data.getApiUrl().trim())) {
            msg.append(MgtCodeEnum.API_URL_FORMAT_ERROR + ";");
        }
        // 校验API路径在表格中是否重复
        final boolean izRepeat = CharSequenceUtil.isNotBlank(data.getApiUrl()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getApiUrl()) && Objects.equals(data.getApiUrl().trim(), m.getApiUrl().trim()));
        if (Boolean.TRUE.equals(izRepeat)) {
            msg.append(MgtCodeEnum.API_URL_EXIST_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getName())) {
            this.checkNameExist(data, msg);
        }
        if (CharSequenceUtil.isNotBlank(data.getApiUrl())) {
            this.checkApiUrlExist(data, msg);
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

    private void checkNameExist(ApiWhiteListImportDTO data, StringBuilder msg) {
        long count = this.apiWhiteListRepository.count(Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .eq(ApiWhiteListDO::getName, data.getName().trim()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(MgtCodeEnum.NAME_EXIST + ";");
        }
    }

    private void checkApiUrlExist(ApiWhiteListImportDTO data, StringBuilder msg) {
        long count = this.apiWhiteListRepository.count(Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .eq(ApiWhiteListDO::getApiUrl, data.getApiUrl().trim()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(MgtCodeEnum.API_URL_EXIST);
        }
    }
}
