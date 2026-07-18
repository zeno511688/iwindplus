/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.setup.domain.dto.WechatConfigMaDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMaEditDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMaSaveDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMaSearchDTO;
import com.iwindplus.setup.domain.vo.WechatConfigMaPageVO;
import com.iwindplus.setup.domain.vo.WechatConfigMaVO;
import com.iwindplus.setup.server.dal.model.WechatConfigMaDO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 微信小程序配置业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface WechatConfigMaService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(WechatConfigMaSaveDTO entity);

    /**
     * 批量保存或编辑.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveOrEditBatch(List<WechatConfigMaDTO> entities);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(WechatConfigMaEditDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, EnableStatusEnum status);

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    boolean editBuildIn(Long id, Boolean buildInFlag);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<WechatConfigMaPageVO>
     */
    IPage<WechatConfigMaPageVO> page(PageDTO<WechatConfigMaDO> page, WechatConfigMaSearchDTO entity);

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return WechatConfigMaVO
     */
    WechatConfigMaVO getByCode(String code);

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode oss模板编码
     * @return WechatConfigMaVO
     */
    WechatConfigMaVO getDetail(Long id, String ossTplCode);

    /**
     * 导出模板.
     *
     * @param response 响应
     */
    void exportTemplate(HttpServletResponse response);

    /**
     * 导入.
     *
     * @param file     文件
     * @param userInfo 当前登录用户信息
     * @param response 响应
     */
    void importByTemplate(MultipartFile file, UserBaseVO userInfo, HttpServletResponse response);
}
