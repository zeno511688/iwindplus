/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.im.domain.dto.ChatGroupUserDTO;
import com.iwindplus.im.domain.dto.ChatGroupUserSaveDTO;
import com.iwindplus.im.server.dal.mapper.ChatGroupUserMapper;
import com.iwindplus.im.server.dal.model.ChatGroupUserDO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聊天群用户聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ChatGroupUserRepository extends JoinCrudRepository<ChatGroupUserMapper, ChatGroupUserDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(ChatGroupUserSaveDTO entity) {
        Long orgId = entity.getOrgId();
        entity.setOrgId(orgId);
        entity.setSeq(this.getNextSeq(entity.getChatGroupId()));
        final ChatGroupUserDO model = BeanUtil.copyProperties(entity, ChatGroupUserDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }


    /**
     * 批量添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<ChatGroupUserSaveDTO> entities) {
        AtomicReference<Integer> seq = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean(true);
        List<ChatGroupUserDTO> saveList = new ArrayList<>(10);
        entities.forEach(entity -> {
            // 判断群用户是否重复
            LambdaQueryWrapper<ChatGroupUserDO> queryWrapper = Wrappers.lambdaQuery(ChatGroupUserDO.class)
                .eq(ChatGroupUserDO::getChatGroupId, entity.getChatGroupId())
                .eq(ChatGroupUserDO::getUserId, entity.getUserId());
            final ChatGroupUserDO data = super.getOne(queryWrapper);
            if (Objects.isNull(data)) {
                if (flag.get()) {
                    seq.set(this.getNextSeq(entity.getChatGroupId()));
                    flag.set(false);
                }
                entity.setSeq(seq.getAndSet(seq.get() + 1));
                saveList.add(entity);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            final List<ChatGroupUserDO> doList = BeanUtil.copyToList(saveList, ChatGroupUserDO.class);
            super.saveBatch(doList, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 查询下一个排序号.
     *
     * @param chatGroupId 聊天群主键
     * @return Integer
     */
    public Integer getNextSeq(Long chatGroupId) {
        QueryWrapper<ChatGroupUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChatGroupUserDO::getChatGroupId, chatGroupId);
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}
