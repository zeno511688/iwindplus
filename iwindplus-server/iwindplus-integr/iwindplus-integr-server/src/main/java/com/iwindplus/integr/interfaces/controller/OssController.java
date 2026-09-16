/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.controller;

import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.dto.OssPreUploadDTO;
import com.iwindplus.integr.api.dto.OssUploadFileExtendDTO;
import com.iwindplus.integr.application.service.OssApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对象存储相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "对象存储接口")
@Slf4j
@RestController
@RequestMapping("admin/integr/oss")
@Validated
@RequiredArgsConstructor
public class OssController extends BaseController {

    private final OssProperty ossProperty;
    private final OssApplicationService ossApplicationService;

    public static final String OSS_TPL_CODE = "c3f67fd355dd6098156053f68385ba34";

    /**
     * 文件预上传（预签名直传）.
     *
     * @param entity 对象
     * @return ResultVO<PreUploadVO>
     */
    @Operation(summary = "文件预上传")
    @PostMapping("preUpload")
    public ResultVO<PreUploadVO> preUpload(@RequestBody @Validated OssPreUploadDTO entity) {
        final String code = ossProperty.getMinio().get(0).getCode();
        final String tplCode = OSS_TPL_CODE;
        entity.setCode(code);
        entity.setTplCode(tplCode);
        entity.setRenamed(false);
        PreUploadVO data = this.ossApplicationService.preUpload(entity);
        return ResultVO.success(data);
    }

    /**
     * 文件上传.
     *
     * @param entity 对象
     * @return ResultVO<UploadVO>
     */
    @Operation(summary = "文件上传")
    @PostMapping("uploadFile")
    public ResultVO<UploadVO> uploadFile(@ModelAttribute @Validated OssUploadFileExtendDTO entity) {
        final String code = ossProperty.getMinio().get(0).getCode();
        final String tplCode = OSS_TPL_CODE;
        entity.setCode(code);
        entity.setTplCode(tplCode);
        entity.setRenamed(true);
        UploadVO data = this.ossApplicationService.uploadFile(entity);
        return ResultVO.success(data);
    }

    /**
     * 文件下载.
     *
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名（可选）
     */
    @Operation(summary = "文件下载")
    @GetMapping("downloadFile")
    public void downloadFile(
        @RequestParam String relativePath,
        @RequestParam(required = false) String fileName) {
        final String code = ossProperty.getMinio().get(0).getCode();
        final String tplCode = OSS_TPL_CODE;
        this.ossApplicationService.downloadFile(code, tplCode, this.getResponse(), relativePath, fileName);
    }
}
