/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.application.service.system.security.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.support.EasyExcelImportVerifyHandler;
import com.iwindplus.mgt.application.service.system.security.dto.IpBlackListImportDTO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * IP黑名单导入校验.
 *
 * @author zengdegui
 * @since 2023/08/23 21:46
 */
public class IpBlackListImportVerifyHandler implements EasyExcelImportVerifyHandler<IpBlackListImportDTO> {

    private IpBlackListRepository ipBlackListRepository;

    private UserBaseVO userInfo;

    /**
     * 缓存数据（用于校验重复）.
     */
    private List<IpBlackListImportDTO> dataList = new ArrayList<>(10);

    public IpBlackListImportVerifyHandler(IpBlackListRepository ipBlackListRepository, UserBaseVO userInfo) {
        this.ipBlackListRepository = ipBlackListRepository;
        this.userInfo = userInfo;
    }

    @Override
    public ExcelVerifyResultVO verifyHandler(IpBlackListImportDTO data) {
        StringBuilder msg = new StringBuilder();
        // 校验IP在表格中是否重复
        final boolean izIpRepeat = CharSequenceUtil.isNotBlank(data.getIp()) && this.dataList.stream()
            .filter(Objects::nonNull)
            .anyMatch(m -> CharSequenceUtil.isNotBlank(m.getIp()) && Objects.equals(data.getIp().trim(), m.getIp().trim()));
        if (Boolean.TRUE.equals(izIpRepeat)) {
            msg.append(MgtCodeEnum.IP_EXIST_IN_TABLE + ";");
        }
        if (CharSequenceUtil.isNotBlank(data.getIp())) {
            this.checkIpExist(data, msg);
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

    private void checkIpExist(IpBlackListImportDTO data, StringBuilder msg) {
        long count = this.ipBlackListRepository.count(Wrappers.lambdaQuery(IpBlackListDO.class)
            .eq(IpBlackListDO::getIp, data.getIp().trim()));
        if (Boolean.TRUE.equals(SqlHelper.retBool(count))) {
            msg.append(MgtCodeEnum.IP_EXIST + ";");
        }
    }
}
