/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.security;

import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubmitDTO;
import com.iwindplus.base.async.task.executor.AsyncTaskExecutor;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ExcelImportResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.oss.support.FileExecuteHandler;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.application.service.common.enums.ExcelImportTplEnum;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListChangeDTO;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListDTO;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListImportDTO;
import com.iwindplus.mgt.application.service.system.security.handler.ApiWhiteListImportVerifyHandler;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.asynctask.security.ApiWhiteListTaskHandler;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * API白名单业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_API_WHITE_LIST})
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ApiWhiteListApplicationService {

    private final ApiWhiteListRepository apiWhiteListRepository;
    private final AsyncTaskExecutor asyncTaskExecutor;
    private final FileExecuteHandler fileExecuteHandler;
    private final Validator validator;

    @CacheEvict(allEntries = true)
    public boolean save(ApiWhiteListDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        // 校验路径是否存在
        this.apiWhiteListRepository.getNameIsExist(entity.getName());
        this.apiWhiteListRepository.getApiUrlIsExist(entity.getApiUrl());
        entity.setSeq(this.apiWhiteListRepository.getNextSeq());
        final ApiWhiteListDO model = BeanUtil.copyProperties(entity, ApiWhiteListDO.class);
        this.apiWhiteListRepository.save(model);
        entity.setId(model.getId());
        // 发送消息
        this.sendMsg(OperateTypeEnum.ADD, List.of(entity.getApiUrl()), null);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean saveOrEditBatch(List<ApiWhiteListDTO> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        final Integer nextSeq = this.apiWhiteListRepository.getNextSeq();
        AtomicInteger seq = new AtomicInteger(nextSeq);
        List<ApiWhiteListDTO> saveList = new ArrayList<>(10);
        List<ApiWhiteListDTO> editList = new ArrayList<>(10);
        entityList.forEach(entity -> {
            final ApiWhiteListDO data = this.apiWhiteListRepository.getOne(Wrappers.lambdaQuery(ApiWhiteListDO.class)
                .eq(ApiWhiteListDO::getApiUrl, entity.getApiUrl().trim()));
            // 为空则添加
            if (Objects.isNull(data)) {
                entity.setStatus(EnableStatusEnum.ENABLE);
                entity.setSeq(seq.incrementAndGet());
                saveList.add(BeanUtil.copyProperties(entity, ApiWhiteListDTO.class));
            } else {
                entity.setId(data.getId());
                entity.setStatus(data.getStatus());
                editList.add(BeanUtil.copyProperties(entity, ApiWhiteListDTO.class));
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            List<ApiWhiteListDO> doList = BeanUtil.copyToList(saveList, ApiWhiteListDO.class);
            this.apiWhiteListRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);

            this.sendMsg(OperateTypeEnum.ADD, saveList.stream().map(ApiWhiteListDTO::getApiUrl).collect(Collectors.toList()), null);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<ApiWhiteListDO> doList = BeanUtil.copyToList(editList, ApiWhiteListDO.class);
            this.apiWhiteListRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);

            Map<EnableStatusEnum, List<String>> statusApiUrlList = doList.stream()
                .collect(Collectors.groupingBy(
                    ApiWhiteListDO::getStatus,
                    Collectors.mapping(ApiWhiteListDO::getApiUrl, Collectors.toList())
                ));
            statusApiUrlList.forEach((status, apiUrlList) -> {
                if (EnableStatusEnum.ENABLE.equals(status)) {
                    this.sendMsg(OperateTypeEnum.MODIFY, apiUrlList, null);
                } else if (EnableStatusEnum.DISABLE.equals(status)
                    || EnableStatusEnum.LOCKED.equals(status)) {
                    this.sendMsg(OperateTypeEnum.DELETE, null, apiUrlList);
                }
            });
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<ApiWhiteListDO> list = this.apiWhiteListRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(ApiWhiteListDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.apiWhiteListRepository.removeByIds(ids);

        // 发送消息
        final List<String> oldApiUrl = list.stream().map(ApiWhiteListDO::getApiUrl).collect(Collectors.toList());
        this.sendMsg(OperateTypeEnum.DELETE, null, oldApiUrl);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean edit(ApiWhiteListDTO entity) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.apiWhiteListRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl()) && !CharSequenceUtil.equals(data.getApiUrl(), entity.getApiUrl().trim())) {
            this.apiWhiteListRepository.getApiUrlIsExist(entity.getApiUrl().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final ApiWhiteListDO model = BeanUtil.copyProperties(entity, ApiWhiteListDO.class);
        this.apiWhiteListRepository.updateById(model);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.MODIFY, List.of(entity.getApiUrl()), List.of(data.getApiUrl()));
        } else if (EnableStatusEnum.DISABLE.equals(entity.getStatus())
            || EnableStatusEnum.LOCKED.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getApiUrl()));
        }

        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ApiWhiteListDO param = new ApiWhiteListDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.apiWhiteListRepository.updateById(param);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(status)) {
            this.sendMsg(OperateTypeEnum.ADD, List.of(data.getApiUrl()), null);
        } else if (EnableStatusEnum.DISABLE.equals(status)
            || EnableStatusEnum.LOCKED.equals(status)) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getApiUrl()));
        }

        return Boolean.TRUE;
    }

    /**
     * 修改为内置数据.
     *
     * @param id          id
     * @param buildInFlag 内置数据标识
     * @return 是否成功
     */
    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ApiWhiteListDO param = new ApiWhiteListDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.apiWhiteListRepository.updateById(param);
        return Boolean.TRUE;
    }

    /**
     * 导出模板.
     *
     * @param response 响应
     */
    public void exportTemplate(HttpServletResponse response) {
        final ExcelImportTplEnum tplEnum = ExcelImportTplEnum.API_WHITE_LIST_TPL;
        final String fileName = new StringBuilder(FileUtil.getPrefix(tplEnum.getDesc()))
            .append(CommonConstant.SymbolConstant.POINT)
            .append(FileUtil.getSuffix(tplEnum.getValue())).toString();
        try {
            Workbook workbook = this.getWorkbook(tplEnum);
            ExcelsUtil.downloadFile(workbook, fileName, response);
        } catch (IOException ex) {
            log.warn(ExceptionConstant.IO_EXCEPTION, ex);
        }
    }

    /**
     * 导入.
     *
     * @param file     文件
     * @param userInfo 当前登录用户信息
     * @param response 响应
     */
    public void importByTemplate(MultipartFile file, UserBaseVO userInfo, HttpServletResponse response) {
        ExcelImportResultVO<ApiWhiteListImportDTO> importResult;
        try {
            importResult = ExcelsUtil.importExcel(file.getInputStream(), this.validator, null, ApiWhiteListImportDTO.class,
                new ApiWhiteListImportVerifyHandler(this.apiWhiteListRepository, userInfo), 2);
        } catch (IOException ex) {
            log.warn(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }

        List<ApiWhiteListImportDTO> failList = importResult.getFailList();
        this.checkExcelData(importResult.getList());
        // 校验数据是否合规
        if (CollUtil.isNotEmpty(failList)) {
            final String sourceFileName = file.getOriginalFilename();
            String fileName = ExcelsUtil.getExcelErrorFile(sourceFileName);
            ExcelsUtil.exportExcel(response, failList, ApiWhiteListImportDTO.class, fileName, null);
        } else {
            // 正确的数据处理
            this.saveRightData(importResult.getRightList(), userInfo);
        }
    }

    private boolean sendMsg(OperateTypeEnum operateType, List<String> newApiUrl, List<String> oldApiUrl) {
        if (CollUtil.isEmpty(newApiUrl)) {
            return false;
        }

        final MessageBaseDTO<ApiWhiteListChangeDTO> messageDTO = new MessageBaseDTO<>();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType("apiWhiteList");

        ApiWhiteListChangeDTO apiWhiteListChangeDTO = new ApiWhiteListChangeDTO();
        apiWhiteListChangeDTO.setNewApiUrl(newApiUrl);
        if (CollUtil.isNotEmpty(oldApiUrl)) {
            apiWhiteListChangeDTO.setOldApiUrl(oldApiUrl);
        }
        messageDTO.setData(apiWhiteListChangeDTO);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncTaskSubmitDTO build = AsyncTaskSubmitDTO.builder()
            .bizName("API白名单数据发送kafka")
            .bizKey("API_WHITE_LIST")
            .bizType("API_WHITE_LIST_PUSH")
            .param(ImmutableMap.of("content", content))
            .executorClass(ApiWhiteListTaskHandler.class)
            .remark("API白名单数据发送kafka")
            .build();
        this.asyncTaskExecutor.submit(build);
        return true;
    }

    private Workbook getWorkbook(ExcelImportTplEnum tplEnum) throws IOException {
        String templateUrl = tplEnum.getValue();
        try (InputStream inputStream = this.fileExecuteHandler.getResource(templateUrl).getInputStream()) {
            return WorkbookFactory.create(inputStream);
        }
    }

    private void checkExcelData(List<ApiWhiteListImportDTO> list) {
        // 校验表格数据是否为空
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.EXCEL_DATA_EMPTY);
        }
        int totalRow = list.size();
        // 校验表格数据行数是不是过大
        if (totalRow > CommonConstant.ExcelConstant.EXCEL_MAX_ROW) {
            throw new BizException(BizCodeEnum.EXCEL_ROW_TOO_BIG);
        }
    }

    private void saveRightData(List<ApiWhiteListImportDTO> rightList, UserBaseVO userInfo) {
        List<ApiWhiteListDTO> entities = new ArrayList<>(10);
        rightList.stream().filter(Objects::nonNull).distinct().forEach(excelData -> {
            ApiWhiteListDTO entity = ApiWhiteListDTO.builder()
                .name(Optional.ofNullable(excelData.getName()).map(String::trim).orElse(null))
                .apiUrl(Optional.ofNullable(excelData.getApiUrl()).map(String::trim).orElse(null))
                .build();
            entities.add(entity);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.saveOrEditBatch(entities);
        }
    }
}
