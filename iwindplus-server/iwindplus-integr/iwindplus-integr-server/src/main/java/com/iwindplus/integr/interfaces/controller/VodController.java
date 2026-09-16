/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.controller;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.factory.VodExecuteHandlerFactory;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
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
@RequestMapping("admin/integr/vod")
@Validated
@RequiredArgsConstructor
public class VodController extends BaseController {

    private final VodProperty vodProperty;
    private final VodExecuteHandlerFactory vodExecuteHandlerFactory;

    /**
     * 视频文件上传.
     *
     * @param file 文件（必填）
     * @return ResultVO < UploadVideoVO>
     */
    @Operation(summary = "视频文件上传")
    @PostMapping("uploadVideo")
    public ResultVO<UploadVideoVO> uploadVideo(@RequestPart MultipartFile file) {
        final String code = vodProperty.getAliyun().get(0).getCode();
        UploadVideoVO data = this.vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, code).uploadVideo(file);
        return ResultVO.success(data);
    }

    /**
     * 视频文件上传2.
     *
     * @param absolutePath 绝对路径（必填）
     * @return ResultVO < UploadVideoVO>
     */
    @Operation(summary = "视频文件上传2")
    @PostMapping("uploadVideoTwo")
    public ResultVO<UploadVideoVO> uploadVideoTwo(@RequestParam String absolutePath) {
        final String code = vodProperty.getAliyun().get(0).getCode();
        UploadVideoVO data = this.vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, code).uploadVideo(new File(absolutePath));
        return ResultVO.success(data);
    }

    /**
     * 获取播放凭证.
     *
     * @param videoId 视频标识（必填）
     * @param timeout 过期时间（可选，单位：分钟，默认：30）
     * @return ResultVO < String>
     */
    @Operation(summary = "获取播放凭证")
    @GetMapping("getPlayAuth")
    public ResultVO<String> getPlayAuth(@RequestParam String videoId, @RequestParam(required = false) Long timeout) {
        final String code = vodProperty.getAliyun().get(0).getCode();
        String data = this.vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, code).getPlayAuth(videoId, timeout);
        return ResultVO.success(data);
    }

    /**
     * 获取视频信息.
     *
     * @param videoId 视频标识（必填）
     * @return ResultVO < GetVideoInfoResponse.Video>
     */
    @Operation(summary = "获取视频信息")
    @GetMapping("getVideoInfo")
    public ResultVO<GetVideoInfoResponse.Video> getVideoInfo(@RequestParam String videoId) {
        final String code = vodProperty.getAliyun().get(0).getCode();
        GetVideoInfoResponse.Video data = this.vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, code).getVideoInfo(videoId);
        return ResultVO.success(data);
    }

    /**
     * 获取源视频信息.
     *
     * @param videoId 视频标识（必填）
     * @return ResultVO < GetMezzanineInfoResponse.Mezzanine>
     */
    @Operation(summary = "获取源视频信息")
    @GetMapping("getSourceVideoInfo")
    public ResultVO<GetMezzanineInfoResponse.Mezzanine> getSourceVideoInfo(@RequestParam String videoId) {
        final String code = vodProperty.getAliyun().get(0).getCode();
        GetMezzanineInfoResponse.Mezzanine data = this.vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, code).getSourceVideoInfo(videoId);
        return ResultVO.success(data);
    }

    /**
     * 删除视频.
     *
     * @param videoIds 视频标识集合（必填）
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "删除视频")
    @DeleteMapping("removeVideo")
    @OperateValid(enabledGa = true)
    public ResultVO<Boolean> removeVideo(@RequestParam List<String> videoIds) {
        final String code = vodProperty.getAliyun().get(0).getCode();
        boolean data = this.vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, code).removeVideo(videoIds);
        return ResultVO.success(data);
    }
}
