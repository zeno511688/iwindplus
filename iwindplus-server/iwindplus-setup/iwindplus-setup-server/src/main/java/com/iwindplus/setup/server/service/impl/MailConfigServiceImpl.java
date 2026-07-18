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
import com.iwindplus.base.domain.enums.YesNoEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.EasyExcelUtil;
import com.iwindplus.base.excel.listener.EasyExcelListener;
import com.iwindplus.base.oss.service.FileService;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.MailConfigDTO;
import com.iwindplus.setup.domain.dto.MailConfigEditDTO;
import com.iwindplus.setup.domain.dto.MailConfigImportDTO;
import com.iwindplus.setup.domain.dto.MailConfigSaveDTO;
import com.iwindplus.setup.domain.dto.MailConfigSearchDTO;
import com.iwindplus.setup.domain.enums.ExcelTemplateEnum;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.MailConfigBaseVO;
import com.iwindplus.setup.domain.vo.MailConfigPageVO;
import com.iwindplus.setup.domain.vo.MailConfigVO;
import com.iwindplus.setup.server.dal.model.MailConfigDO;
import com.iwindplus.setup.server.dal.repository.MailConfigRepository;
import com.iwindplus.setup.server.service.MailConfigService;
import com.iwindplus.setup.server.service.handler.MailConfigImportVerifyHandler;
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
 * 邮箱配置业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_CONFIG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailConfigServiceImpl implements MailConfigService {

    private final MailConfigRepository mailConfigRepository;
    private final Validator validator;
    private final FileService fileService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(MailConfigSaveDTO entity) {
        this.mailConfigRepository.getNameIsExist(entity.getName(), entity.getOrgId());
        this.mailConfigRepository.getUsernameIsExist(entity.getUsername().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setSslEnable(Boolean.TRUE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final MailConfigDO model = BeanUtil.copyProperties(entity, MailConfigDO.class);
        this.mailConfigRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveOrEditBatch(List<MailConfigDTO> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        List<MailConfigDTO> saveList = new ArrayList<>(10);
        List<MailConfigDTO> editList = new ArrayList<>(10);
        entityList.forEach(entity -> {
            final MailConfigDO data = this.mailConfigRepository.getOne(Wrappers.lambdaQuery(MailConfigDO.class)
                .eq(MailConfigDO::getOrgId, entity.getName())
                .and(w -> w.eq(MailConfigDO::getName, entity.getName()).or().eq(MailConfigDO::getUsername, entity.getUsername().trim())));
            // 为空则添加
            if (Objects.isNull(data)) {
                String code = IdUtil.simpleUUID();
                entity.setCode(code);
                entity.setStatus(EnableStatusEnum.ENABLE);
                saveList.add(entity);
            } else {
                entity.setId(data.getId());
                editList.add(entity);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            final List<MailConfigDO> doList = BeanUtil.copyToList(saveList, MailConfigDO.class);
            this.mailConfigRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);
        }
        if (CollUtil.isNotEmpty(editList)) {
            final List<MailConfigDO> doList = BeanUtil.copyToList(editList, MailConfigDO.class);
            this.mailConfigRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<MailConfigDO> data = this.mailConfigRepository.listByIds(ids);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.mailConfigRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(MailConfigEditDTO entity) {
        // 编辑
        MailConfigDO data = this.mailConfigRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.mailConfigRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getUsername()) && !CharSequenceUtil.equals(data.getUsername(), entity.getUsername().trim())) {
            this.mailConfigRepository.getUsernameIsExist(entity.getUsername().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.mailConfigRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final MailConfigDO model = BeanUtil.copyProperties(entity, MailConfigDO.class);
        this.mailConfigRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        MailConfigDO data = this.mailConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MailConfigDO entity = new MailConfigDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.mailConfigRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        MailConfigDO data = this.mailConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MailConfigDO param = new MailConfigDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.mailConfigRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<MailConfigPageVO> page(PageDTO<MailConfigDO> page, MailConfigSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<MailConfigDO> queryWrapper = Wrappers.lambdaQuery(MailConfigDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(MailConfigDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(MailConfigDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(MailConfigDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getNickName())) {
            queryWrapper.eq(MailConfigDO::getNickName, entity.getNickName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getUsername())) {
            queryWrapper.eq(MailConfigDO::getUsername, entity.getUsername().trim());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            queryWrapper.eq(MailConfigDO::getOrgId, entity.getOrgId());
        }
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
        queryWrapper.select(MailConfigDO::getId, MailConfigDO::getCreatedTime, MailConfigDO::getCreatedTimestamp, MailConfigDO::getCreatedBy,
            MailConfigDO::getModifiedTime, MailConfigDO::getModifiedTimestamp, MailConfigDO::getModifiedBy, MailConfigDO::getVersion,
            MailConfigDO::getRemark, MailConfigDO::getStatus, MailConfigDO::getCode, MailConfigDO::getName,
            MailConfigDO::getNickName, MailConfigDO::getHost, MailConfigDO::getUsername, MailConfigDO::getSslEnable, MailConfigDO::getRetryEnable);
        final PageDTO<MailConfigDO> modelPage = this.mailConfigRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailConfigPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MailConfigVO getByCode(String code) {
        MailConfigDO data = this.mailConfigRepository.getOne(Wrappers.lambdaQuery(MailConfigDO.class)
            .eq(MailConfigDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.MAIL_CONFIG_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.MAIL_CONFIG_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.MAIL_CONFIG_LOCKED);
        }
        final MailConfigVO result = BeanUtil.copyProperties(data, MailConfigVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<MailConfigBaseVO> listEnabled() {
        final List<MailConfigDO> list = this.mailConfigRepository.list(Wrappers.lambdaQuery(MailConfigDO.class)
            .eq(MailConfigDO::getStatus, EnableStatusEnum.ENABLE)
            .select(MailConfigDO::getId, MailConfigDO::getCode, MailConfigDO::getName)
            .orderByDesc(MailConfigDO::getCreatedTime));
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, MailConfigBaseVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MailConfigVO getDetail(Long id) {
        MailConfigDO data = this.mailConfigRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final MailConfigVO result = BeanUtil.copyProperties(data, MailConfigVO.class);
        return result;
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        final String fileName = new StringBuilder(FileUtil.getPrefix(ExcelTemplateEnum.MAIL_CONFIG_TEMPLATE.getDesc()))
            .append(CommonConstant.SymbolConstant.POINT)
            .append(FileUtil.getSuffix(ExcelTemplateEnum.MAIL_CONFIG_TEMPLATE.getValue())).toString();
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
        final String sourceFileName = file.getOriginalFilename();
        EasyExcelListener<MailConfigImportDTO> importResult;
        try {
            importResult = EasyExcelUtil.importExcel(file.getInputStream(), this.validator, null, MailConfigImportDTO.class,
                new MailConfigImportVerifyHandler(this.mailConfigRepository, userInfo), 2);
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }
        List<MailConfigImportDTO> failList = importResult.getFailList();
        this.checkExcelData(importResult.getList());
        // 校验数据是否合规
        if (CollUtil.isNotEmpty(failList)) {
            String fileName = ExcelsUtil.getExcelErrorFile(sourceFileName);
            EasyExcelUtil.exportExcel(response, failList, MailConfigImportDTO.class, fileName, null);
        } else {
            // 正确的数据处理
            this.saveRightData(importResult.getRightList(), userInfo);
        }
    }

    private Workbook getWorkbook() throws IOException {
        String templateUrl = ExcelTemplateEnum.MAIL_CONFIG_TEMPLATE.getValue();
        try (InputStream inputStream = this.fileService.getResource(templateUrl).getInputStream()) {
            final Workbook workbook = WorkbookFactory.create(inputStream);
            final List<String> yesNoList = Arrays.stream(YesNoEnum.values()).map(m -> m.getDesc()).collect(Collectors.toCollection(ArrayList::new));
            String[] templates = yesNoList.toArray(new String[yesNoList.size()]);
            ExcelsUtil.dropDownOption(workbook, Arrays.asList(templates), 'G', 2, null);
            ExcelsUtil.dropDownOption(workbook, Arrays.asList(templates), 'H', 2, null);
            return workbook;
        }
    }

    private void saveRightData(List<MailConfigImportDTO> rightList, UserBaseVO userInfo) {
        List<MailConfigDTO> entities = new ArrayList<>(10);
        rightList.stream().filter(Objects::nonNull).distinct().forEach(excelData -> {
            MailConfigDO entity = MailConfigDO.builder()
                .name(Optional.ofNullable(excelData.getName()).map(String::trim).orElse(null))
                .nickName(Optional.ofNullable(excelData.getNickName()).map(String::trim).orElse(null))
                .host(Optional.ofNullable(excelData.getHost()).map(String::trim).orElse(null))
                .username(Optional.ofNullable(excelData.getUsername()).map(String::trim).orElse(null))
                .password(Optional.ofNullable(excelData.getPassword()).map(String::trim).orElse(null))
                .port(Optional.ofNullable(excelData.getPort()).map(m -> Integer.valueOf(excelData.getPort())).orElse(null))
                .sslEnable(Optional.ofNullable(BaseEnum.fromDesc(excelData.getSslEnable(), YesNoEnum.class).getValue() > 0).orElse(Boolean.FALSE))
                .retryEnable(Optional.ofNullable(BaseEnum.fromDesc(excelData.getRetryEnable(), YesNoEnum.class).getValue() > 0).orElse(Boolean.FALSE))
                .remark(Optional.ofNullable(excelData.getRemark()).map(String::trim).orElse(null))
                .orgId(userInfo.getOrgId())
                .build();
            final MailConfigDTO dto = BeanUtil.copyProperties(entity, MailConfigDTO.class);
            entities.add(dto);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.saveOrEditBatch(entities);
        }
    }

    private void checkExcelData(List<MailConfigImportDTO> list) {
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
