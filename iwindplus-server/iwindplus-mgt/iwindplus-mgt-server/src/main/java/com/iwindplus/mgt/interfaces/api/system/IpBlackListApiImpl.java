/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.IpBlackListApi;
import com.iwindplus.mgt.api.system.dto.IpBlackListSearchDTO;
import com.iwindplus.mgt.api.system.vo.IpBlackListPageVO;
import com.iwindplus.mgt.application.query.system.security.IpBlackListQueryService;
import com.iwindplus.mgt.application.service.system.security.IpBlackListApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IP黑名单相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class IpBlackListApiImpl implements IpBlackListApi {

    private final IpBlackListApplicationService ipBlackListApplicationService;
    private final IpBlackListQueryService ipBlackListQueryService;

    @Override
    public ResultVO<List<String>> listIp() {
        List<String> data = this.ipBlackListQueryService.listIp();
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<DbPageVO<IpBlackListPageVO>> page(IpBlackListSearchDTO entity) {
        IPage<IpBlackListPageVO> data = this.ipBlackListQueryService.page(entity);
        final DbPageVO<IpBlackListPageVO> result = new DbPageVO<>(data.getCurrent(), data.getSize(), data.getTotal(),
            data.getRecords());
        return ResultVO.success(result);
    }
}
