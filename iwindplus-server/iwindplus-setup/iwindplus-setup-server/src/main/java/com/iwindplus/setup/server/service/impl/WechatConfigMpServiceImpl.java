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
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.excel.EasyExcelUtil;
import com.iwindplus.base.excel.listener.EasyExcelListener;
import com.iwindplus.base.oss.service.FileService;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.WechatConfigMpDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMpEditDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMpImportDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMpSaveDTO;
import com.iwindplus.setup.domain.dto.WechatConfigMpSearchDTO;
import com.iwindplus.setup.domain.enums.ExcelTemplateEnum;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.WechatConfigMpPageVO;
import com.iwindplus.setup.domain.vo.WechatConfigMpVO;
import com.iwindplus.setup.server.dal.model.WechatConfigMpDO;
import com.iwindplus.setup.server.dal.repository.WechatConfigMpRepository;
import com.iwindplus.setup.server.service.WechatConfigMpService;
import com.iwindplus.setup.server.service.handler.WechatConfigMpImportVerifyHandler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * 微信公众号配置业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/4/30
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_WECHAT_CONFIG_MP})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class WechatConfigMpServiceImpl implements WechatConfigMpService {

    private final WechatConfigMpRepository wechatConfigMpRepository;
    private final Validator validator;
    private final FileService fileService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(WechatConfigMpSaveDTO entity) {
        this.wechatConfigMpRepository.getNameIsExist(entity.getName(), entity.getOrgId());
        this.wechatConfigMpRepository.getAccessKeyIsExist(entity.getAccessKey().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final WechatConfigMpDO model = BeanUtil.copyProperties(entity, WechatConfigMpDO.class);
        this.wechatConfigMpRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveOrEditBatch(List<WechatConfigMpDTO> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return false;
        }
        List<WechatConfigMpDTO> saveList = new ArrayList<>(10);
        List<WechatConfigMpDTO> editList = new ArrayList<>(10);
        entityList.forEach(entity -> {
            final WechatConfigMpDO data = this.wechatConfigMpRepository.getOne(Wrappers.lambdaQuery(WechatConfigMpDO.class)
                .and(w -> w.eq(WechatConfigMpDO::getName, entity.getName()).or().eq(WechatConfigMpDO::getAccessKey, entity.getAccessKey().trim())));
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
            List<WechatConfigMpDO> doList = BeanUtil.copyToList(saveList, WechatConfigMpDO.class);
            this.wechatConfigMpRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<WechatConfigMpDO> doList = BeanUtil.copyToList(editList, WechatConfigMpDO.class);
            this.wechatConfigMpRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<WechatConfigMpDO> list = this.wechatConfigMpRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.wechatConfigMpRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(WechatConfigMpEditDTO entity) {
        // 编辑
        WechatConfigMpDO data = this.wechatConfigMpRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.wechatConfigMpRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey()) && !CharSequenceUtil.equals(data.getAccessKey(), entity.getAccessKey().trim())) {
            this.wechatConfigMpRepository.getAccessKeyIsExist(entity.getAccessKey().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.wechatConfigMpRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final WechatConfigMpDO model = BeanUtil.copyProperties(entity, WechatConfigMpDO.class);
        this.wechatConfigMpRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        WechatConfigMpDO data = this.wechatConfigMpRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        WechatConfigMpDO entity = new WechatConfigMpDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.wechatConfigMpRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        WechatConfigMpDO data = this.wechatConfigMpRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        WechatConfigMpDO param = new WechatConfigMpDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.wechatConfigMpRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<WechatConfigMpPageVO> page(PageDTO<WechatConfigMpDO> page, WechatConfigMpSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<WechatConfigMpDO> queryWrapper = Wrappers.lambdaQuery(WechatConfigMpDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(WechatConfigMpDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(WechatConfigMpDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(WechatConfigMpDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey())) {
            queryWrapper.eq(WechatConfigMpDO::getAccessKey, entity.getAccessKey().trim());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            queryWrapper.eq(WechatConfigMpDO::getOrgId, entity.getOrgId());
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
        queryWrapper.select(WechatConfigMpDO::getId, WechatConfigMpDO::getCreatedTime, WechatConfigMpDO::getCreatedTimestamp,
            WechatConfigMpDO::getCreatedBy, WechatConfigMpDO::getModifiedTime, WechatConfigMpDO::getModifiedTimestamp,
            WechatConfigMpDO::getModifiedBy, WechatConfigMpDO::getBuildInFlag, WechatConfigMpDO::getVersion, WechatConfigMpDO::getRemark,
            WechatConfigMpDO::getStatus, WechatConfigMpDO::getCode, WechatConfigMpDO::getName, WechatConfigMpDO::getAccessKey,
            WechatConfigMpDO::getNotifyUrl, WechatConfigMpDO::getNotifySuccessUrl
        );
        final PageDTO<WechatConfigMpDO> modelPage = this.wechatConfigMpRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, WechatConfigMpPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public WechatConfigMpVO getByCode(String code) {
        WechatConfigMpDO data = this.wechatConfigMpRepository.getOne(Wrappers.lambdaQuery(WechatConfigMpDO.class)
            .eq(WechatConfigMpDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.WECHAT_CONFIG_MP_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.WECHAT_CONFIG_MP_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.WECHAT_CONFIG_MP_LOCKED);
        }
        WechatConfigMpVO result = BeanUtil.copyProperties(data, WechatConfigMpVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public WechatConfigMpVO getDetail(Long id) {
        WechatConfigMpDO data = this.wechatConfigMpRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        WechatConfigMpVO result = BeanUtil.copyProperties(data, WechatConfigMpVO.class);
        return result;
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        final String fileName = new StringBuilder(FileUtil.getPrefix(ExcelTemplateEnum.WECHAT_CONFIG_MP_TEMPLATE.getDesc()))
            .append(CommonConstant.SymbolConstant.POINT)
            .append(FileUtil.getSuffix(ExcelTemplateEnum.WECHAT_CONFIG_MP_TEMPLATE.getValue())).toString();
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
        EasyExcelListener<WechatConfigMpImportDTO> importResult;
        try {
            importResult = EasyExcelUtil.importExcel(file.getInputStream(), this.validator, null, WechatConfigMpImportDTO.class,
                new WechatConfigMpImportVerifyHandler(this.wechatConfigMpRepository, userInfo), 2);
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }
        List<WechatConfigMpImportDTO> failList = importResult.getFailList();
        this.checkExcelData(importResult.getList());
        // 校验数据是否合规
        if (CollUtil.isNotEmpty(failList)) {
            final String sourceFileName = file.getOriginalFilename();
            String fileName = ExcelsUtil.getExcelErrorFile(sourceFileName);
            EasyExcelUtil.exportExcel(response, failList, WechatConfigMpImportDTO.class, fileName, null);
        } else {
            // 正确的数据处理
            this.saveRightData(importResult.getRightList(), userInfo);
        }
    }

    private Workbook getWorkbook() throws IOException {
        String templateUrl = ExcelTemplateEnum.WECHAT_CONFIG_MP_TEMPLATE.getValue();
        try (InputStream inputStream = this.fileService.getResource(templateUrl).getInputStream()) {
            return WorkbookFactory.create(inputStream);
        }
    }

    private void saveRightData(List<WechatConfigMpImportDTO> rightList, UserBaseVO userInfo) {
        List<WechatConfigMpDO> entities = new ArrayList<>(10);
        rightList.stream().filter(Objects::nonNull).distinct().forEach(excelData -> {
            WechatConfigMpDO entity = WechatConfigMpDO.builder()
                .name(Optional.ofNullable(excelData.getName()).map(String::trim).orElse(null))
                .accessKey(Optional.ofNullable(excelData.getAccessKey()).map(String::trim).orElse(null))
                .secretKey(Optional.ofNullable(excelData.getSecretKey()).map(String::trim).orElse(null))
                .token(Optional.ofNullable(excelData.getToken()).map(String::trim).orElse(null))
                .aesKey(Optional.ofNullable(excelData.getAesKey()).map(String::trim).orElse(null))
                .notifyUrl(Optional.ofNullable(excelData.getNotifyUrl()).map(String::trim).orElse(null))
                .notifySuccessUrl(Optional.ofNullable(excelData.getNotifySuccessUrl()).map(String::trim).orElse(null))
                .remark(Optional.ofNullable(excelData.getRemark()).map(String::trim).orElse(null))
                .orgId(userInfo.getOrgId())
                .build();
            entities.add(entity);
        });
        if (CollUtil.isNotEmpty(entities)) {
            List<WechatConfigMpDTO> dtoList = BeanUtil.copyToList(entities, WechatConfigMpDTO.class);
            this.saveOrEditBatch(dtoList);
        }
    }

    private void checkExcelData(List<WechatConfigMpImportDTO> list) {
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
