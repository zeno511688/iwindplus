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
import cn.hutool.crypto.SecureUtil;
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
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.EasyExcelUtil;
import com.iwindplus.base.excel.listener.EasyExcelListener;
import com.iwindplus.base.oss.service.FileService;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.OssConfigDTO;
import com.iwindplus.setup.domain.dto.OssConfigEditDTO;
import com.iwindplus.setup.domain.dto.OssConfigImportDTO;
import com.iwindplus.setup.domain.dto.OssConfigSaveDTO;
import com.iwindplus.setup.domain.dto.OssConfigSearchDTO;
import com.iwindplus.setup.domain.enums.ExcelTemplateEnum;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.OssConfigBaseVO;
import com.iwindplus.setup.domain.vo.OssConfigPageVO;
import com.iwindplus.setup.domain.vo.OssConfigVO;
import com.iwindplus.setup.server.dal.model.OssConfigDO;
import com.iwindplus.setup.server.dal.repository.OssConfigRepository;
import com.iwindplus.setup.server.service.OssConfigService;
import com.iwindplus.setup.server.service.handler.OssConfigImportVerifyHandler;
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
import net.dreamlu.mica.core.utils.StringUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储配置业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/4/30
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_OSS_CONFIG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OssConfigServiceImpl implements OssConfigService {

    private final OssConfigRepository ossConfigRepository;
    private final Validator validator;
    private final FileService fileService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(OssConfigSaveDTO entity) {
        this.ossConfigRepository.getNameIsExist(entity.getName(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final OssConfigDO model = BeanUtil.copyProperties(entity, OssConfigDO.class);
        this.ossConfigRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveOrEditBatch(List<OssConfigDTO> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        List<OssConfigDTO> saveList = new ArrayList<>(10);
        List<OssConfigDTO> editList = new ArrayList<>(10);
        entityList.forEach(entity -> {
            final OssConfigDO data = this.ossConfigRepository.getOne(Wrappers.lambdaQuery(OssConfigDO.class)
                .and(w -> w.eq(OssConfigDO::getName, entity.getName())));
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
            List<OssConfigDO> doList = BeanUtil.copyToList(saveList, OssConfigDO.class);
            this.ossConfigRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<OssConfigDO> doList = BeanUtil.copyToList(editList, OssConfigDO.class);
            this.ossConfigRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<OssConfigDO> data = this.ossConfigRepository.listByIds(ids);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.ossConfigRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(OssConfigEditDTO entity) {
        // 编辑
        OssConfigDO data = this.ossConfigRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.ossConfigRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.ossConfigRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final OssConfigDO model = BeanUtil.copyProperties(entity, OssConfigDO.class);
        this.ossConfigRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        OssConfigDO data = this.ossConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OssConfigDO entity = new OssConfigDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.ossConfigRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        OssConfigDO data = this.ossConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OssConfigDO param = new OssConfigDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.ossConfigRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<OssConfigPageVO> page(PageDTO<OssConfigDO> page, OssConfigSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<OssConfigDO> queryWrapper = Wrappers.lambdaQuery(OssConfigDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(OssConfigDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(OssConfigDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(OssConfigDO::getCode, entity.getCode().trim());
        }
        if (Objects.nonNull(entity.getType())) {
            queryWrapper.eq(OssConfigDO::getType, entity.getType());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey())) {
            queryWrapper.eq(OssConfigDO::getAccessKey, entity.getAccessKey().trim());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            queryWrapper.eq(OssConfigDO::getOrgId, entity.getOrgId());
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
        queryWrapper.select(OssConfigDO::getId, OssConfigDO::getCreatedTime, OssConfigDO::getCreatedTimestamp, OssConfigDO::getCreatedBy,
            OssConfigDO::getModifiedTime, OssConfigDO::getModifiedTimestamp, OssConfigDO::getModifiedBy, OssConfigDO::getVersion,
            OssConfigDO::getRemark, OssConfigDO::getBuildInFlag, OssConfigDO::getStatus, OssConfigDO::getCode, OssConfigDO::getName,
            OssConfigDO::getType, OssConfigDO::getOssEndpoint, OssConfigDO::getAccessKey
        );
        PageDTO<OssConfigDO> modelPage = this.ossConfigRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, OssConfigPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public OssConfigVO getByCode(String code) {
        OssConfigDO data = this.ossConfigRepository.getOne(Wrappers.lambdaQuery(OssConfigDO.class)
            .eq(OssConfigDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.OSS_CONFIG_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.OSS_CONFIG_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.OSS_CONFIG_LOCKED);
        }
        OssConfigVO result = BeanUtil.copyProperties(data, OssConfigVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<OssConfigBaseVO> listEnabled() {
        final List<OssConfigDO> list = this.ossConfigRepository.list(Wrappers.lambdaQuery(OssConfigDO.class)
            .eq(OssConfigDO::getStatus, EnableStatusEnum.ENABLE)
            .select(OssConfigDO::getId, OssConfigDO::getCode, OssConfigDO::getName)
            .orderByDesc(OssConfigDO::getCreatedTime));
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, OssConfigBaseVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public OssConfigVO getDetail(Long id) {
        OssConfigDO data = this.ossConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        OssConfigVO result = BeanUtil.copyProperties(data, OssConfigVO.class);
        return result;
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        final String fileName = new StringBuilder(FileUtil.getPrefix(ExcelTemplateEnum.OSS_CONFIG_TEMPLATE.getDesc()))
            .append(CommonConstant.SymbolConstant.POINT)
            .append(FileUtil.getSuffix(ExcelTemplateEnum.OSS_CONFIG_TEMPLATE.getValue())).toString();
        try {
            Workbook workbook = this.getWorkbook();
            ExcelsUtil.downloadFile(workbook, fileName, response);
        } catch (IOException ex) {
            log.warn(ExceptionConstant.IO_EXCEPTION, ex);
        }
    }

    @CacheEvict(allEntries = true)
    @Override
    public void importByTemplate(MultipartFile file, UserBaseVO userInfo, HttpServletResponse response) {
        EasyExcelListener<OssConfigImportDTO> importResult;
        try {
            importResult = EasyExcelUtil.importExcel(file.getInputStream(), this.validator, null, OssConfigImportDTO.class,
                new OssConfigImportVerifyHandler(this.ossConfigRepository, userInfo), 2);
        } catch (IOException ex) {
            log.warn(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }
        List<OssConfigImportDTO> failList = importResult.getFailList();
        this.checkExcelData(importResult.getList());
        // 校验数据是否合规
        if (CollUtil.isNotEmpty(failList)) {
            final String sourceFileName = file.getOriginalFilename();
            String fileName = ExcelsUtil.getExcelErrorFile(sourceFileName);
            EasyExcelUtil.exportExcel(response, failList, OssConfigImportDTO.class, fileName, null);
        } else {
            // 正确的数据处理
            this.saveRightData(importResult.getRightList(), userInfo);
        }
    }

    private Workbook getWorkbook() throws IOException {
        String templateUrl = ExcelTemplateEnum.OSS_CONFIG_TEMPLATE.getValue();
        try (InputStream inputStream = this.fileService.getResource(templateUrl).getInputStream()) {
            final Workbook workbook = WorkbookFactory.create(inputStream);
            final List<String> ossTypeList = Arrays.stream(OssTypeEnum.values()).map(m -> m.getDesc())
                .collect(Collectors.toCollection(ArrayList::new));
            String[] templates = ossTypeList.toArray(new String[ossTypeList.size()]);
            ExcelsUtil.dropDownOption(workbook, Arrays.asList(templates), 'B', 2, null);
            return workbook;
        }
    }

    private void saveRightData(List<OssConfigImportDTO> rightList, UserBaseVO userInfo) {
        List<OssConfigDO> entities = new ArrayList<>(10);
        rightList.stream().filter(Objects::nonNull).distinct().forEach(excelData -> {
            OssConfigDO entity = OssConfigDO.builder()
                .name(Optional.ofNullable(excelData.getName()).map(String::trim).orElse(null))
                .type(CharSequenceUtil.isNotBlank(excelData.getType()) ? BaseEnum.fromDesc(excelData.getType(), OssTypeEnum.class) : null)
                .ossEndpoint(Optional.ofNullable(excelData.getOssEndpoint()).map(String::trim).orElse(null))
                .accessKey(Optional.ofNullable(excelData.getAccessKey()).map(String::trim).orElse(null))
                .secretKey(Optional.ofNullable(excelData.getSecretKey()).map(String::trim).orElse(null))
                .stsEndpoint(Optional.ofNullable(excelData.getStsEndpoint()).map(String::trim).orElse(null))
                .roleArn(Optional.ofNullable(excelData.getRoleArn()).map(String::trim).orElse(null))
                .remark(Optional.ofNullable(excelData.getRemark()).map(String::trim).orElse(null))
                .orgId(userInfo.getOrgId())
                .build();
            entities.add(entity);
        });
        if (CollUtil.isNotEmpty(entities)) {
            List<OssConfigDTO> dtoList = BeanUtil.copyToList(entities, OssConfigDTO.class);
            this.saveOrEditBatch(dtoList);
        }
    }

    private void checkExcelData(List<OssConfigImportDTO> list) {
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
