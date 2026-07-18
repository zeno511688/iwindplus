/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.api;

import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
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
     * 文件上传（字节数组）.
     *
     * @param entity 对象
     * @return ResultVO < UploadVO>
     */
    @Operation(summary = "文件上传（字节数组）")
    @PostMapping(API_PREFIX + "uploadByte")
    ResultVO<UploadVO> uploadByte(@RequestBody @Validated OssUploadByteDTO entity);

    /**
     * 获取访问路径.
     *
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（单位：分钟，默认：60）
     * @return ResultVO<List < FilePathVO>>
     */
    @Operation(summary = "获取访问路径")
    @GetMapping(API_PREFIX + "listSignUrl")
    ResultVO<List<FilePathVO>> listSignUrl(
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "relativePaths") List<String> relativePaths,
        @RequestParam(value = "timeout", required = false) Integer timeout);

    /**
     * 批量删除文件.
     *
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     */
    @Operation(summary = "批量删除文件")
    @DeleteMapping(API_PREFIX + "removeFiles")
    void removeFiles(
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "relativePaths") List<String> relativePaths);
}
