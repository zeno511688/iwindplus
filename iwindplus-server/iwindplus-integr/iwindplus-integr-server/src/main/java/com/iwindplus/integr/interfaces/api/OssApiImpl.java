/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.api;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.OssApi;
import com.iwindplus.integr.api.dto.OssPreUploadDTO;
import com.iwindplus.integr.api.dto.OssUploadFileDTO;
import com.iwindplus.integr.api.dto.OssUploadFileExtendDTO;
import com.iwindplus.integr.application.service.OssApplicationService;
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

    private final OssApplicationService ossService;

    @Override
    public ResultVO<PreUploadVO> preUpload(OssPreUploadDTO entity) {
        PreUploadVO result = this.ossService.preUpload(entity);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<UploadVO> uploadFile(OssUploadFileDTO entity) {
        OssUploadFileExtendDTO extendDTO = BeanUtil.copyProperties(entity, OssUploadFileExtendDTO.class);
        UploadVO result = this.ossService.uploadFile(extendDTO);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<List<FilePathVO>> listSignUrl(String code, String tplCode, List<String> relativePaths, Integer timeout) {
        List<FilePathVO> data = this.ossService.listSignUrl(code, tplCode, relativePaths, timeout);
        return ResultVO.success(data);
    }

    @Override
    public void removeFiles(String code, String tplCode, List<String> relativePaths) {
        this.ossService.removeFiles(code, tplCode, relativePaths);
    }
}
