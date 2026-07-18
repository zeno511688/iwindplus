/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BaseEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.EasyExcelUtil;
import com.iwindplus.base.excel.listener.EasyExcelListener;
import com.iwindplus.base.oss.service.FileService;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.VodConfigDTO;
import com.iwindplus.setup.domain.dto.VodConfigEditDTO;
import com.iwindplus.setup.domain.dto.VodConfigImportDTO;
import com.iwindplus.setup.domain.dto.VodConfigSaveDTO;
import com.iwindplus.setup.domain.dto.VodConfigSearchDTO;
import com.iwindplus.setup.domain.enums.ExcelTemplateEnum;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.VodConfigPageVO;
import com.iwindplus.setup.domain.vo.VodConfigVO;
import com.iwindplus.setup.server.dal.model.VodConfigDO;
import com.iwindplus.setup.server.dal.repository.VodConfigRepository;
import com.iwindplus.setup.server.service.VodConfigService;
import com.iwindplus.setup.server.service.handler.VodConfigImportVerifyHandler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频点播配置业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/4/30
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_VOD_CONFIG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class VodConfigServiceImpl implements VodConfigService {

    private final VodConfigRepository vodConfigRepository;
    private final Validator validator;
    private final FileService fileService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(VodConfigSaveDTO entity) {
        this.vodConfigRepository.getNameIsExist(entity.getName(), entity.getType(), entity.getOrgId());
        this.vodConfigRepository.getRegionIsExist(entity.getRegion().trim(), entity.getType(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final VodConfigDO model = BeanUtil.copyProperties(entity, VodConfigDO.class);
        this.vodConfigRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveOrEditBatch(List<VodConfigDTO> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        List<VodConfigDTO> saveList = new ArrayList<>(10);
        List<VodConfigDTO> editList = new ArrayList<>(10);
        entityList.forEach(entity -> {
            final VodConfigDO data = this.vodConfigRepository.getOne(Wrappers.lambdaQuery(VodConfigDO.class)
                .and(w -> w.eq(VodConfigDO::getName, entity.getName()).or().eq(VodConfigDO::getRegion, entity.getRegion().trim())));
            // 为空则添加
            if (Objects.isNull(data)) {
                entity.setCode(IdUtil.simpleUUID());
                entity.setStatus(EnableStatusEnum.ENABLE);
                saveList.add(entity);
            } else {
                entity.setId(data.getId());
                editList.add(entity);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            List<VodConfigDO> doList = BeanUtil.copyToList(saveList, VodConfigDO.class);
            this.vodConfigRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<VodConfigDO> doList = BeanUtil.copyToList(editList, VodConfigDO.class);
            this.vodConfigRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<VodConfigDO> list = this.vodConfigRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.vodConfigRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(VodConfigEditDTO entity) {
        // 编辑
        VodConfigDO data = this.vodConfigRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.vodConfigRepository.getNameIsExist(entity.getName().trim(), data.getType(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRegion()) && !CharSequenceUtil.equals(data.getRegion(), entity.getRegion().trim())) {
            this.vodConfigRepository.getRegionIsExist(entity.getRegion().trim(), data.getType(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.vodConfigRepository.getCodeIsExist(entity.getCode().trim(), data.getType(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final VodConfigDO model = BeanUtil.copyProperties(entity, VodConfigDO.class);
        this.vodConfigRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        VodConfigDO data = this.vodConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        VodConfigDO entity = new VodConfigDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.vodConfigRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        VodConfigDO data = this.vodConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        VodConfigDO param = new VodConfigDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.vodConfigRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<VodConfigPageVO> page(PageDTO<VodConfigDO> page, VodConfigSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<VodConfigDO> queryWrapper = Wrappers.lambdaQuery(VodConfigDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(VodConfigDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(VodConfigDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(VodConfigDO::getCode, entity.getCode().trim());
        }
        if (Objects.nonNull(entity.getType())) {
            queryWrapper.eq(VodConfigDO::getType, entity.getType());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRegion())) {
            queryWrapper.eq(VodConfigDO::getRegion, entity.getRegion().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey())) {
            queryWrapper.eq(VodConfigDO::getAccessKey, entity.getAccessKey().trim());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            queryWrapper.eq(VodConfigDO::getOrgId, entity.getOrgId());
        }
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(CommonConstant.DbConstant.MODIFIED_TIME);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(VodConfigDO::getId, VodConfigDO::getCreatedTime, VodConfigDO::getCreatedTimestamp, VodConfigDO::getCreatedBy,
            VodConfigDO::getModifiedTime, VodConfigDO::getModifiedTimestamp, VodConfigDO::getModifiedBy, VodConfigDO::getBuildInFlag,
            VodConfigDO::getVersion, VodConfigDO::getRemark, VodConfigDO::getStatus, VodConfigDO::getCode, VodConfigDO::getName, VodConfigDO::getType,
            VodConfigDO::getRegion, VodConfigDO::getAccessKey
        );
        final PageDTO<VodConfigDO> modelPage = this.vodConfigRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, VodConfigPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public VodConfigVO getByCode(String code) {
        VodConfigDO data = this.vodConfigRepository.getOne(Wrappers.lambdaQuery(VodConfigDO.class)
            .eq(VodConfigDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.VOD_CONFIG_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.VOD_CONFIG_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.VOD_CONFIG_LOCKED);
        }
        VodConfigVO result = BeanUtil.copyProperties(data, VodConfigVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public VodConfigVO getDetail(Long id) {
        VodConfigDO data = this.vodConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        VodConfigVO result = BeanUtil.copyProperties(data, VodConfigVO.class);
        return result;
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        final String fileName = new StringBuilder(FileUtil.getPrefix(ExcelTemplateEnum.VOD_CONFIG_TEMPLATE.getDesc()))
            .append(CommonConstant.SymbolConstant.POINT)
            .append(FileUtil.getSuffix(ExcelTemplateEnum.VOD_CONFIG_TEMPLATE.getValue())).toString();
        try {
            Workbook workbook = this.getWorkbook();
            ExcelsUtil.downloadFile(workbook, fileName, response);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
    }

    @CacheEvict(allEntries = true)
    @Override
    public void importByTemplate(MultipartFile file, UserBaseVO userInfo, HttpServletResponse response) {
        EasyExcelListener<VodConfigImportDTO> importResult;
        try {
            importResult = EasyExcelUtil.importExcel(file.getInputStream(), this.validator, null, VodConfigImportDTO.class,
                new VodConfigImportVerifyHandler(this.vodConfigRepository, userInfo), 2);
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }
        List<VodConfigImportDTO> failList = importResult.getFailList();
        this.checkExcelData(importResult.getList());
        // 校验数据是否合规
        if (CollUtil.isNotEmpty(failList)) {
            final String sourceFileName = file.getOriginalFilename();
            String fileName = ExcelsUtil.getExcelErrorFile(sourceFileName);
            EasyExcelUtil.exportExcel(response, failList, VodConfigImportDTO.class, fileName, null);
        } else {
            // 正确的数据处理
            this.saveRightData(importResult.getRightList(), userInfo);
        }
    }

    private Workbook getWorkbook() throws IOException {
        String templateUrl = ExcelTemplateEnum.VOD_CONFIG_TEMPLATE.getValue();
        try (InputStream inputStream = this.fileService.getResource(templateUrl).getInputStream()) {
            final Workbook workbook = WorkbookFactory.create(inputStream);
            final List<String> vodTypeList = Arrays.stream(OssTypeEnum.values()).map(m -> m.getDesc())
                .collect(Collectors.toCollection(ArrayList::new));
            String[] templates = vodTypeList.toArray(new String[vodTypeList.size()]);
            ExcelsUtil.dropDownOption(workbook, Arrays.asList(templates), 'B', 2, null);
            return workbook;
        }
    }

    private void saveRightData(List<VodConfigImportDTO> rightList, UserBaseVO userInfo) {
        List<VodConfigDO> entities = new ArrayList<>(10);
        rightList.stream().filter(Objects::nonNull).distinct().forEach(excelData -> {
            VodConfigDO entity = VodConfigDO.builder()
                .name(Optional.ofNullable(excelData.getName()).map(String::trim).orElse(null))
                .type(CharSequenceUtil.isNotBlank(excelData.getType()) ? BaseEnum.fromDesc(excelData.getType(), VodTypeEnum.class) : null)
                .region(Optional.ofNullable(excelData.getRegion()).map(String::trim).orElse(null))
                .accessKey(Optional.ofNullable(excelData.getAccessKey()).map(String::trim).orElse(null))
                .secretKey(Optional.ofNullable(excelData.getSecretKey()).map(String::trim).orElse(null))
                .stsEndpoint(Optional.ofNullable(excelData.getStsEndpoint()).map(String::trim).orElse(null))
                .roleArn(Optional.ofNullable(excelData.getRoleArn()).map(String::trim).orElse(null))
                .notifyUrl(Optional.ofNullable(excelData.getNotifyUrl()).map(String::trim).orElse(null))
                .remark(Optional.ofNullable(excelData.getRemark()).map(String::trim).orElse(null))
                .orgId(userInfo.getOrgId())
                .build();
            entities.add(entity);
        });
        if (CollUtil.isNotEmpty(entities)) {
            List<VodConfigDTO> dtoList = BeanUtil.copyToList(entities, VodConfigDTO.class);
            this.saveOrEditBatch(dtoList);
        }
    }

    private void checkExcelData(List<VodConfigImportDTO> list) {
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

}
