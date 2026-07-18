/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
import com.iwindplus.setup.server.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "对象存储接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/oss")
@Validated
@RequiredArgsConstructor
public class OssController extends BaseController {

    private final OssService ossService;

    /**
     * 文件上传.
     *
     * @param entity 对象
     * @param file   附件（必填）
     * @return ResultVO<UploadVO>
     */
    @Operation(summary = "文件上传")
    @PostMapping("uploadFile")
    public ResultVO<UploadVO> uploadFile(
        @ModelAttribute @Valid OssUploadByteDTO entity,
        @RequestPart MultipartFile file) {
        final String requestId = this.getRequestId();
        final UploadByteDTO uploadBytes = FilesUtil.getUploadBytes(file);
        entity.setRequestId(requestId);
        entity.setAttachment(uploadBytes);
        UploadVO data = this.ossService.uploadByte(entity);
        return ResultVO.success(data);
    }

    /**
     * 文件下载.
     *
     * @param tplCode      模板编码（必填）
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名（可选）
     */
    @Operation(summary = "文件下载")
    @GetMapping("downloadFile")
    public void downloadFile(
        @RequestParam String tplCode,
        @RequestParam String relativePath,
        @RequestParam(required = false) String fileName) {
        this.ossService.downloadFile(tplCode, this.getResponse(), relativePath, fileName);
    }

    /**
     * 批量获取访问路径.
     *
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（可选，单位：分钟，默认：60）
     * @return ResultVO<List < FilePathVO>>
     */
    @Operation(summary = "批量获取访问路径")
    @GetMapping("listSignUrl")
    public ResultVO<List<FilePathVO>> listSignUrl(
        @RequestParam String tplCode,
        @RequestParam List<String> relativePaths,
        @RequestParam(required = false) Integer timeout) {
        List<FilePathVO> data = this.ossService.listSignUrl(tplCode, relativePaths, timeout);
        return ResultVO.success(data);
    }

    /**
     * 批量删除文件.
     *
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     * @return ResponseEntity<ResultVO < Boolean>>
     */
    @Operation(summary = "批量删除文件")
    @DeleteMapping("removeFiles")
    @OperateValid(enabledGa = true)
    public void removeFiles(@RequestParam String tplCode, @RequestParam List<String> relativePaths) {
        this.ossService.removeFiles(tplCode, relativePaths);
    }
}
