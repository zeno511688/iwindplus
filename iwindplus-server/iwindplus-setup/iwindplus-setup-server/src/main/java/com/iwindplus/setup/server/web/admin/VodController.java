/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.server.service.VodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频点播相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "视频点播接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/vod")
@Validated
@RequiredArgsConstructor
public class VodController extends BaseController {

    private final VodService vodService;

    /**
     * 视频文件上传.
     *
     * @param code 配置编码（必填）
     * @param file 文件（必填）
     * @return ResultVO < UploadVideoVO>
     */
    @Operation(summary = "视频文件上传")
    @PostMapping("uploadVideo")
    public ResultVO<UploadVideoVO> uploadVideo(@RequestParam String code, @RequestPart MultipartFile file) {
        UploadVideoVO data = this.vodService.uploadVideo(code, file);
        return ResultVO.success(data);
    }

    /**
     * 视频文件上传2.
     *
     * @param code         配置编码（必填）
     * @param absolutePath 绝对路径（必填）
     * @return ResultVO < UploadVideoVO>
     */
    @Operation(summary = "视频文件上传2")
    @PostMapping("uploadVideoTwo")
    public ResultVO<UploadVideoVO> uploadVideoTwo(@RequestParam String code, @RequestParam String absolutePath) {
        UploadVideoVO data = this.vodService.uploadVideoTwo(code, absolutePath);
        return ResultVO.success(data);
    }

    /**
     * 获取播放凭证.
     *
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @param timeout 过期时间（可选，单位：分钟，默认：30）
     * @return ResultVO < String>
     */
    @Operation(summary = "获取播放凭证")
    @GetMapping("getPlayAuth")
    public ResultVO<String> getPlayAuth(@RequestParam String code, @RequestParam String videoId, @RequestParam(required = false) Long timeout) {
        String data = this.vodService.getPlayAuth(code, videoId, timeout);
        return ResultVO.success(data);
    }

    /**
     * 获取视频信息.
     *
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @return ResultVO < GetVideoInfoResponse.Video>
     */
    @Operation(summary = "获取视频信息")
    @GetMapping("getVideoInfo")
    public ResultVO<GetVideoInfoResponse.Video> getVideoInfo(@RequestParam String code, @RequestParam String videoId) {
        GetVideoInfoResponse.Video data = this.vodService.getVideoInfo(code, videoId);
        return ResultVO.success(data);
    }

    /**
     * 获取源视频信息.
     *
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @return ResultVO < GetMezzanineInfoResponse.Mezzanine>
     */
    @Operation(summary = "获取源视频信息")
    @GetMapping("getSourceVideoInfo")
    public ResultVO<GetMezzanineInfoResponse.Mezzanine> getSourceVideoInfo(@RequestParam String code, @RequestParam String videoId) {
        GetMezzanineInfoResponse.Mezzanine data = this.vodService.getSourceVideoInfo(code, videoId);
        return ResultVO.success(data);
    }

    /**
     * 删除视频.
     *
     * @param code     配置编码（必填）
     * @param videoIds 视频标识集合（必填）
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "删除视频")
    @DeleteMapping("removeVideo")
    @OperateValid(enabledGa = true)
    public ResultVO<Boolean> removeVideo(@RequestParam String code, @RequestParam List<String> videoIds) {
        boolean data = this.vodService.removeVideo(code, videoIds);
        return ResultVO.success(data);
    }
}
