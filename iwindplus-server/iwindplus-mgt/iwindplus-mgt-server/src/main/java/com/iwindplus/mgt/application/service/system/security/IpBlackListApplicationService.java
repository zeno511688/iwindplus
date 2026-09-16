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
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListImportDTO;
import com.iwindplus.mgt.application.service.system.security.dto.IpBlackListChangeDTO;
import com.iwindplus.mgt.application.service.system.security.dto.IpBlackListDTO;
import com.iwindplus.mgt.application.service.system.security.dto.IpBlackListImportDTO;
import com.iwindplus.mgt.application.service.system.security.handler.IpBlackListImportVerifyHandler;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.asynctask.security.ApiWhiteListTaskHandler;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListRepository;
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
 * IP黑名单业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_IP_BLACK_LIST})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class IpBlackListApplicationService {

    private final IpBlackListRepository ipBlackListRepository;
    private final AsyncTaskExecutor asyncTaskExecutor;
    private final FileExecuteHandler fileExecuteHandler;
    private final Validator validator;

    @CacheEvict(allEntries = true)
    public boolean save(IpBlackListDTO entity) {
        this.ipBlackListRepository.getIpIsExist(entity.getIp().trim());
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setSeq(this.ipBlackListRepository.getNextSeq());
        IpBlackListDO model = BeanUtil.copyProperties(entity, IpBlackListDO.class);
        this.ipBlackListRepository.save(model);
        entity.setId(model.getId());
        // 发送消息
        this.sendMsg(OperateTypeEnum.ADD, List.of(entity.getIp()), null);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean saveOrEditBatch(List<IpBlackListDTO> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        final Integer nextSeq = this.ipBlackListRepository.getNextSeq();
        AtomicInteger seq = new AtomicInteger(nextSeq);
        List<IpBlackListDTO> saveList = new ArrayList<>(10);
        List<IpBlackListDTO> editList = new ArrayList<>(10);
        entityList.forEach(entity -> {
            final IpBlackListDO data = this.ipBlackListRepository.getOne(
                Wrappers.lambdaQuery(IpBlackListDO.class)
                    .eq(IpBlackListDO::getIp, entity.getIp()));
            // 为空则添加
            if (Objects.isNull(data)) {
                entity.setStatus(EnableStatusEnum.ENABLE);
                entity.setSeq(seq.incrementAndGet());
                saveList.add(entity);
            } else {
                entity.setId(data.getId());
                entity.setStatus(data.getStatus());
                editList.add(entity);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            List<IpBlackListDO> doList = BeanUtil.copyToList(saveList, IpBlackListDO.class);
            this.ipBlackListRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);

            this.sendMsg(OperateTypeEnum.ADD, saveList.stream().map(IpBlackListDTO::getIp).collect(Collectors.toList()), null);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<IpBlackListDO> doList = BeanUtil.copyToList(editList, IpBlackListDO.class);
            this.ipBlackListRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);

            Map<EnableStatusEnum, List<String>> statusIpList= doList.stream()
                .collect(Collectors.groupingBy(
                    IpBlackListDO::getStatus,
                    Collectors.mapping(IpBlackListDO::getIp, Collectors.toList())
                ));
            statusIpList.forEach((status, ipList) -> {
                if (EnableStatusEnum.ENABLE.equals(status)) {
                    this.sendMsg(OperateTypeEnum.MODIFY, ipList, null);
                } else if (EnableStatusEnum.DISABLE.equals(status)
                    || EnableStatusEnum.LOCKED.equals(status)) {
                    this.sendMsg(OperateTypeEnum.DELETE, null, ipList);
                }
            });
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<IpBlackListDO> list = this.ipBlackListRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.ipBlackListRepository.removeByIds(ids);

        // 发送消息
        final List<String> oldIp = list.stream().map(IpBlackListDO::getIp).collect(Collectors.toList());
        this.sendMsg(OperateTypeEnum.DELETE, null, oldIp);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean edit(IpBlackListDTO entity) {
        IpBlackListDO data = this.ipBlackListRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp()) && !CharSequenceUtil.equals(data.getIp(), entity.getIp().trim())) {
            this.ipBlackListRepository.getIpIsExist(entity.getIp().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        IpBlackListDO model = BeanUtil.copyProperties(entity, IpBlackListDO.class);
        this.ipBlackListRepository.updateById(model);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.MODIFY, List.of(entity.getIp()), List.of(data.getIp()));
        } else if (EnableStatusEnum.DISABLE.equals(entity.getStatus())
            || EnableStatusEnum.LOCKED.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getIp()));
        }

        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        IpBlackListDO data = this.ipBlackListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        IpBlackListDO entity = new IpBlackListDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.ipBlackListRepository.updateById(entity);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(status)) {
            this.sendMsg(OperateTypeEnum.ADD, List.of(data.getIp()), null);
        } else if (EnableStatusEnum.DISABLE.equals(status)
            || EnableStatusEnum.LOCKED.equals(status)) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getIp()));
        }

        return Boolean.TRUE;
    }

    /**
     * 修改为内置数据.
     *
     * @param id          id
     * @param buildInFlag 内置数据标识
     * @return true
     */
    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        IpBlackListDO data = this.ipBlackListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        IpBlackListDO param = new IpBlackListDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.ipBlackListRepository.updateById(param);
        return Boolean.TRUE;
    }

    /**
     * 导出模板.
     *
     * @param response 响应
     */
    public void exportTemplate(HttpServletResponse response) {
        final ExcelImportTplEnum tplEnum = ExcelImportTplEnum.IP_BLACK_LIST_TPL;
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
        ExcelImportResultVO<IpBlackListImportDTO> importResult;
        try {
            importResult = ExcelsUtil.importExcel(file.getInputStream(), this.validator, null, IpBlackListImportDTO.class,
                new IpBlackListImportVerifyHandler(this.ipBlackListRepository, userInfo), 2);
        } catch (IOException ex) {
            log.warn(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }

        List<IpBlackListImportDTO> failList = importResult.getFailList();
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

    private boolean sendMsg(OperateTypeEnum operateType, List<String> newIp, List<String> oldIp) {
        if (CollUtil.isEmpty(newIp)) {
            return false;
        }

        final MessageBaseDTO<IpBlackListChangeDTO> messageDTO = new MessageBaseDTO<>();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType("ipBlackList");

        IpBlackListChangeDTO ipBlackListChangeDTO = new IpBlackListChangeDTO();
        ipBlackListChangeDTO.setNewIp(newIp);
        if (CollUtil.isNotEmpty(oldIp)) {
            ipBlackListChangeDTO.setOldIp(oldIp);
        }
        messageDTO.setData(ipBlackListChangeDTO);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncTaskSubmitDTO build = AsyncTaskSubmitDTO.builder()
            .bizName("IP黑名单数据发送kafka")
            .bizKey("IP_BLACK_LIST")
            .bizType("IP_BLACK_PUSH")
            .param(ImmutableMap.of("content", content))
            .executorClass(ApiWhiteListTaskHandler.class)
            .remark("IP黑名单数据发送kafka")
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

    private void checkExcelData(List<IpBlackListImportDTO> list) {
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

    private void saveRightData(List<IpBlackListImportDTO> rightList, UserBaseVO userInfo) {
        List<IpBlackListDTO> entities = new ArrayList<>(10);
        rightList.stream().filter(Objects::nonNull).distinct().forEach(excelData -> {
            IpBlackListDTO entity = IpBlackListDTO.builder()
                .ip(Optional.ofNullable(excelData.getIp()).map(String::trim).orElse(null))
                .build();
            entities.add(entity);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.saveOrEditBatch(entities);
        }
    }
}
