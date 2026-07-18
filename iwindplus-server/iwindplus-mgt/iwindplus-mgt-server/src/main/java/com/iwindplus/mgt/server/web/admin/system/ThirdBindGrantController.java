/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.web.admin.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSearchDTO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantUserDTO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantVO;
import com.iwindplus.mgt.server.service.system.ThirdBindGrantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三方绑定授权相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "第三方绑定授权接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/thirdBindGrant")
@Validated
@RequiredArgsConstructor
public class ThirdBindGrantController extends BaseController {

    private final ThirdBindGrantService thirdBindGrantService;

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "thirdBindGrant", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除第三方绑定授权")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.thirdBindGrantService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 绑定用户.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "绑定用户")
    @PutMapping("editUser")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.code",
        "#entity.mobile"}, bizType = "thirdBindGrant", operateType = "editUser", operateName = "绑定用户", operateDesc = "第三方绑定用户")
    public ResultVO<Boolean> editUser(@RequestBody @Validated ThirdBindGrantUserDTO entity) {
        boolean data = this.thirdBindGrantService.editUser(entity);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < ThirdBindGrantVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<ThirdBindGrantVO>> page(@Validated ThirdBindGrantSearchDTO entity) {
        IPage<ThirdBindGrantVO> data = this.thirdBindGrantService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < ThirdBindGrantVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<ThirdBindGrantVO> getDetail(@RequestParam Long id) {
        ThirdBindGrantVO data = this.thirdBindGrantService.getDetail(id);
        return ResultVO.success(data);
    }
}
