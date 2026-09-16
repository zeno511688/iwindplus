/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api;

import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.integr.api.dto.OssPreUploadDTO;
import com.iwindplus.integr.api.dto.OssUploadFileDTO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 对象存储相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface OssApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/oss/";

    /**
     * 文件预上传（预签名直传）.
     *
     * @param entity 对象
     * @return ResultVO < PreUploadVO>
     */
    @Operation(summary = "文件预上传（预签名直传）")
    @PostMapping(API_PREFIX + "preUpload")
    ResultVO<PreUploadVO> preUpload(@RequestBody @Validated OssPreUploadDTO entity);

    /**
     * 文件上传.
     *
     * @param entity 对象
     * @return ResultVO < UploadVO>
     */
    @Operation(summary = "文件上传")
    @PostMapping(API_PREFIX + "uploadFile")
    ResultVO<UploadVO> uploadFile(@RequestBody @Validated OssUploadFileDTO entity);

    /**
     * 获取访问路径.
     *
     * @param code          配置编码（必填）
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（单位：分钟，默认：60）
     * @return ResultVO<List < FilePathVO>>
     */
    @Operation(summary = "获取访问路径")
    @GetMapping(API_PREFIX + "listSignUrl")
    ResultVO<List<FilePathVO>> listSignUrl(
        @RequestParam(value = "code") String code,
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "relativePaths") List<String> relativePaths,
        @RequestParam(value = "timeout", required = false) Integer timeout);

    /**
     * 批量删除文件.
     *
     * @param code          配置编码（必填）
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     */
    @Operation(summary = "批量删除文件")
    @DeleteMapping(API_PREFIX + "removeFiles")
    void removeFiles(
        @RequestParam(value = "code") String code,
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "relativePaths") List<String> relativePaths);
}
