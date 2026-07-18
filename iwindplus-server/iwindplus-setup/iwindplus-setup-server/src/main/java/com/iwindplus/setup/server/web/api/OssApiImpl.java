/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.api;

import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.api.OssApi;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
import com.iwindplus.setup.server.service.OssService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对象存储相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class OssApiImpl extends BaseController implements OssApi {

    private final OssService ossService;

    @Override
    public ResultVO<UploadVO> uploadByte(OssUploadByteDTO entity) {
        UploadVO result = this.ossService.uploadByte(entity);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<List<FilePathVO>> listSignUrl(String tplCode, List<String> relativePaths, Integer timeout) {
        List<FilePathVO> data = this.ossService.listSignUrl(tplCode, relativePaths, timeout);
        return ResultVO.success(data);
    }

    @Override
    public void removeFiles(String tplCode, List<String> relativePaths) {
        this.ossService.removeFiles(tplCode, relativePaths);
    }
}
