/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.mgt.domain.dto.system.I18nMsgQueryDTO;
import com.iwindplus.mgt.domain.dto.system.I18nMsgSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.vo.system.I18nMsgExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgPageVO;
import com.iwindplus.mgt.server.dal.mapper.system.I18nMsgMapper;
import com.iwindplus.mgt.server.dal.model.system.I18nMsgDO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 国际化消息聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
@RequiredArgsConstructor
public class I18nMsgRepository extends JoinCrudRepository<I18nMsgMapper, I18nMsgDO> {

    /**
     * 获取编码是否已存在.
     *
     * @param code      编码
     * @param projectId 项目主键
     */
    public void getCodeIsExist(String code, Long projectId) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(I18nMsgDO.class)
            .eq(I18nMsgDO::getProjectId, projectId)
            .eq(I18nMsgDO::getCode, code)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @param projectId 项目主键
     * @return Integer
     */
    public Integer getNextSeq(Long projectId) {
        QueryWrapper<I18nMsgDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(I18nMsgDO::getProjectId, projectId);
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 分页查询.
     *
     * @param entity 对象
     * @return List<I18nMsgExtendVO>
     */
    public IPage<I18nMsgPageVO> selectPageByCondition(I18nMsgSearchDTO entity) {
        return super.getBaseMapper().selectPageByCondition(entity);
    }

    /**
     * 通过条件获取国际化消息.
     *
     * @param entity 对象
     * @return List<I18nMsgExtendVO>
     */
    public List<I18nMsgExtendVO> listByCondition(I18nMsgQueryDTO entity) {
        return super.getBaseMapper().selectListByCondition(entity);
    }

    /**
     * 构建国际化消息列表.
     *
     * @param content   内容
     * @param projectId 项目主键
     * @return List<I18nMsgDO>
     */
    public List<I18nMsgDO> buildI18nMsgList(String content, Long projectId) {
        if (CharSequenceUtil.isBlank(content)) {
            return null;
        }

        final Map<String, String> map = HttpsUtil.contentToMap(content);
        if (MapUtil.isEmpty(map)) {
            return null;
        }

        final List<I18nMsgDO> list = map.entrySet().stream()
            .filter(entry -> CharSequenceUtil.isNotBlank(entry.getKey())
                && CharSequenceUtil.isNotBlank(entry.getValue()))
            .map(entry -> I18nMsgDO.builder()
                .projectId(projectId)
                .code(entry.getKey())
                .value(entry.getValue())
                .build())
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        boolean hasLongValue = list.stream()
            .anyMatch(item -> CharSequenceUtil.isNotBlank(item.getValue())
                && item.getValue().length() > 200);
        if (hasLongValue) {
            throw new BizException(MgtCodeEnum.I18N_MSG_VALUE_TOO_LONG);
        }
        return this.buildI18nMsgDOList(list);
    }

    /**
     * 构造国际化消息列表.
     *
     * @param entities 国际化消息列表
     * @return List<I18nMsgDO>
     */
    public List<I18nMsgDO> buildI18nMsgDOList(List<I18nMsgDO> entities) {
        Long projectId = entities.get(0).getProjectId();

        List<I18nMsgDO> existingList = super.list(Wrappers.lambdaQuery(I18nMsgDO.class)
            .eq(I18nMsgDO::getProjectId, projectId)
            .isNotNull(I18nMsgDO::getCode)
            .ne(I18nMsgDO::getCode, CharSequenceUtil.EMPTY)
            .select(I18nMsgDO::getId, I18nMsgDO::getProjectId,
                I18nMsgDO::getCode, I18nMsgDO::getValue, I18nMsgDO::getSeq));
        Map<String, I18nMsgDO> existingMap = existingList.stream()
            .filter(item -> item != null && item.getCode() != null)
            .collect(Collectors.toMap(I18nMsgDO::getCode, Function.identity()));
        int maxSeq = existingList.stream()
            .filter(Objects::nonNull)
            .map(I18nMsgDO::getSeq)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0);
        int nextSeq = maxSeq + 1;

        List<I18nMsgDO> resultList = new ArrayList<>(entities.size());

        for (I18nMsgDO entity : entities) {
            I18nMsgDO existing = existingMap.get(entity.getCode());
            if (existing == null) {
                entity.setSeq(nextSeq++);
                resultList.add(entity);
            } else {
                if (!Objects.equals(existing.getValue(), entity.getValue())) {
                    existing.setValue(entity.getValue());
                }
                resultList.add(existing);
            }
        }

        return resultList;
    }

    /**
     * 构建内容.
     *
     * @param list 国际化消息列表
     * @return String
     */
    public String buildContent(List<I18nMsgExtendVO> list) {
        return list.stream().filter(item -> CharSequenceUtil.isNotBlank(item.getCode())
                && CharSequenceUtil.isNotBlank(item.getValue())
                && EnableStatusEnum.ENABLE.equals(item.getStatus()))
            .map(item -> item.getCode().trim() + SymbolConstant.EQUAL + item.getValue().trim())
            .collect(Collectors.joining(SymbolConstant.NEWLINE));
    }
}
