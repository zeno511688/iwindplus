/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskSubmitDTO;
import com.iwindplus.base.export.task.domain.vo.ExportTaskSubmitVO;
import com.iwindplus.base.export.task.executor.ExportTaskExecutor;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.query.system.security.ApiWhiteListQueryService;
import com.iwindplus.mgt.application.query.system.security.dto.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.application.query.system.security.vo.ApiWhiteListPageVO;
import com.iwindplus.mgt.application.query.system.security.vo.ApiWhiteListVO;
import com.iwindplus.mgt.application.service.system.security.ApiWhiteListApplicationService;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListDTO;
import com.iwindplus.mgt.application.service.system.security.handler.ApiWhiteListExportTaskHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * API白名单相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "API白名单接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/apiWhiteList")
@Validated
@RequiredArgsConstructor
public class ApiWhiteListController extends BaseController {

    private final ApiWhiteListApplicationService apiWhiteListApplicationService;
    private final ApiWhiteListQueryService apiWhiteListQueryService;
    private final ExportTaskExecutor exportTaskExecutor;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加API白名单")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "apiWhiteList", operateType = "save", operateName = "添加", operateDesc = "添加API白名单")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) ApiWhiteListDTO entity) {
        boolean data = this.apiWhiteListApplicationService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "apiWhiteList", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除API白名单")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.apiWhiteListApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑")
    @PutMapping("edit")
    @OperateLog(keys = "#entity.id", bizType = "apiWhiteList", operateType = "edit", operateName = "编辑", operateDesc = "编辑API白名单")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) ApiWhiteListDTO entity) {
        boolean data = this.apiWhiteListApplicationService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑状态")
    @PutMapping("editStatus")
    @OperateLog(keys = {"#id"}, bizType = "apiWhiteList", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑API白名单状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.apiWhiteListApplicationService.editStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑设为内置")
    @PutMapping("editBuildIn")
    @OperateLog(keys = {"#id"}, bizType = "apiWhiteList", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑API白名单设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.apiWhiteListApplicationService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < ApiWhiteListPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<ApiWhiteListPageVO>> page(@Validated ApiWhiteListSearchDTO entity) {
        IPage<ApiWhiteListPageVO> data = this.apiWhiteListQueryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<ApiWhiteListVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<ApiWhiteListVO> getDetail(@RequestParam Long id) {
        ApiWhiteListVO data = this.apiWhiteListQueryService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 导出模版.
     *
     * @param response 响应
     */
    @Operation(summary = "导出模版")
    @GetMapping("exportTemplate")
    public void exportTemplate(HttpServletResponse response) {
        this.apiWhiteListApplicationService.exportTemplate(response);
    }

    /**
     * 导入.
     *
     * @param file     文件
     * @param response 响应
     */
    @Operation(summary = "导入")
    @PostMapping("importByTemplate")
    public void importByTemplate(@RequestPart MultipartFile file, HttpServletResponse response) {
        UserBaseVO userInfo = this.getUserInfo();
        this.apiWhiteListApplicationService.importByTemplate(file, userInfo, response);
    }

    /**
     * 提交导出任务.
     *
     * @param entity 对象
     * @return ResultVO<ExportTaskSubmitVO>
     */
    @Operation(summary = "提交导出任务")
    @PostMapping("exportTask")
    public ResultVO<ExportTaskSubmitVO> exportTask(@RequestBody @Validated ApiWhiteListSearchDTO entity) {
        final ExportTaskSubmitDTO param = ExportTaskSubmitDTO
            .builder()
            .executorClass(ApiWhiteListExportTaskHandler.class)
            .build();
        param.setQueryParam(entity);

        final ExportTaskSubmitVO data = this.exportTaskExecutor.submit(param);
        return ResultVO.success(data);
    }
}
