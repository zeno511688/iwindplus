/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseTreeCheckedVO;
import com.iwindplus.base.domain.vo.BaseTreeVO;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.application.query.upms.permission.vo.MenuBaseListSystemVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.MenuExtendVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.MenuListSystemVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.MenuTreeSystemVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.MenuTreeVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.MenuVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.ResourceBaseCheckedVO;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.MenuDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.MenuRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 菜单查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MENU})
@RequiredArgsConstructor
public class MenuQueryService {

    private final MenuRepository menuRepository;
    private final ResourceRepository resourceRepository;
    private final OssClient ossClient;
    private final MgtProperty property;

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public List<Tree<Long>> listBySystemId(Long systemId) {
        return this.listBySystemId(systemId, null);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public List<Tree<Long>> listEnabledBySystemId(Long systemId) {
        return this.listBySystemId(systemId, EnableStatusEnum.ENABLE);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    public List<MenuTreeSystemVO> listByUserId(Long orgId, Long userId) {
        List<MenuTreeSystemVO> result = new ArrayList<>(16);
        final List<MenuListSystemVO> list = this.menuRepository.getBaseMapper().selectListByUserId(orgId, userId);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        list.stream().filter(Objects::nonNull).forEach(data -> this.buildMenuListSystem(result, data, this.listTree(data.getMenus(), 0L)));
        result.sort(Comparator.comparing(MenuTreeSystemVO::getSeq));
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    public List<MenuTreeSystemVO> listByRoleId(Long orgId, Long roleId) {
        List<MenuTreeSystemVO> result = new ArrayList<>(16);
        final List<MenuBaseListSystemVO> allList = this.menuRepository.getBaseMapper().selectListEnabled();
        if (CollUtil.isEmpty(allList)) {
            return null;
        }

        final List<ResourceBaseCheckedVO> resourceBaseCheckedList = this.resourceRepository.getBaseMapper().selectListEnabled();
        this.appendData(allList, resourceBaseCheckedList);

        if (Objects.nonNull(roleId)) {
            final List<MenuBaseListSystemVO> checkedList = this.menuRepository.getBaseMapper().selectListByRoleId(orgId, roleId);
            if (CollUtil.isNotEmpty(checkedList)) {
                List<ResourceBaseCheckedVO> resourceBaseCheckedListByRoleId = this.resourceRepository.getBaseMapper()
                    .selectListByRoleId(orgId, roleId);
                this.appendData(checkedList, resourceBaseCheckedListByRoleId);
                this.buildMenuTree(result, allList, checkedList);
            }
        } else {
            allList.stream().filter(Objects::nonNull)
                .forEach(data -> this.buildMenuBaseListSystem(result, data, this.listBaseTreeChecked(data.getMenus(), 0L)));
        }
        result.sort(Comparator.comparing(MenuTreeSystemVO::getSeq));
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public MenuVO getDetail(Long id) {
        MenuDO data = this.menuRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, MenuVO.class);
    }

    public MenuExtendVO getDetailExtend(Long id) {
        MenuVO data = this.getDetail(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        MenuExtendVO result = BeanUtil.copyProperties(data, MenuExtendVO.class);
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getIconUrl())) {
            relativePaths.add(data.getIconUrl());
        }
        List<FilePathVO> filePaths = OrgApplicationService.getFilePaths(
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        if (CollUtil.isNotEmpty(filePaths)) {
            filePaths.forEach(p -> {
                if (CharSequenceUtil.isNotBlank(data.getIconUrl()) && data.getIconUrl().equals(p.getRelativePath())) {
                    result.setIconUrlStr(p.getAbsolutePath());
                }
            });
        }
        return result;
    }

    private List<Tree<Long>> listBySystemId(Long systemId, EnableStatusEnum status) {
        List<BaseTreeVO> list = this.menuRepository.getBaseMapper().selectListBySystemId(systemId, status);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        final List<BaseTreeCheckedVO> allList = BeanUtil.copyToList(list, BaseTreeCheckedVO.class);
        return this.listBaseTreeChecked(allList, 0L);
    }

    private void appendData(List<MenuBaseListSystemVO> sourceList, List<ResourceBaseCheckedVO> appendList) {
        if (CollUtil.isEmpty(appendList)) {
            return;
        }
        Map<Long, List<ResourceBaseCheckedVO>> resourceBaseCheckedMap = appendList.stream()
            .filter(Objects::nonNull)
            .filter(r -> r.getMenuId() != null)
            .collect(Collectors.groupingBy(ResourceBaseCheckedVO::getMenuId));
        // 资源加入菜单中
        sourceList.parallelStream()
            .filter(m -> CollUtil.isNotEmpty(m.getMenus()))
            .forEach(menuBaseListSystemVO -> {
                List<BaseTreeCheckedVO> menus = menuBaseListSystemVO.getMenus();
                List<BaseTreeCheckedVO> resourceTrees = menus.stream()
                    .filter(Objects::nonNull)
                    .flatMap(menu -> resourceBaseCheckedMap.getOrDefault(menu.getId(), List.of())
                        .stream().filter(Objects::nonNull)
                        .map(res -> BaseTreeCheckedVO.builder()
                            .id(res.getId())
                            .code(res.getCode())
                            .name(res.getName())
                            .type(res.getType())
                            .level((menu.getLevel() != null ? menu.getLevel() : 0) + 1)
                            .seq(res.getSeq())
                            .parentId(res.getMenuId())
                            .checked(res.getChecked())
                            .build()))
                    .collect(Collectors.toList());
                menus.addAll(resourceTrees);
            });
    }

    private void buildMenuTree(List<MenuTreeSystemVO> list, List<MenuBaseListSystemVO> result,
        List<MenuBaseListSystemVO> checkedList) {
        List<MenuBaseListSystemVO> tempList = new ArrayList<>(10);
        result.stream().filter(Objects::nonNull).forEach(map -> checkedList.stream()
            .filter(m -> Objects.equals(map.getId(), m.getId())).forEach(data -> {
                final List<BaseTreeCheckedVO> menus = this.listWithChecked(map.getMenus(),
                    data.getMenus());
                this.buildMenuBaseListSystem(list, map, this.listBaseTreeChecked(menus, 0L));
                tempList.add(map);
            }));
        result.removeAll(tempList);
        result.forEach(data -> this.buildMenuBaseListSystem(list, data, this.listBaseTreeChecked(data.getMenus(), 0L)));
    }

    private void buildMenuBaseListSystem(List<MenuTreeSystemVO> list, MenuBaseListSystemVO entity, List<Tree<Long>> menus) {
        final MenuTreeSystemVO build = MenuTreeSystemVO.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .seq(entity.getSeq())
            .menus(menus)
            .build();
        list.add(build);
    }

    private void buildMenuListSystem(List<MenuTreeSystemVO> list, MenuListSystemVO entity, List<Tree<Long>> menus) {
        final MenuTreeSystemVO build = MenuTreeSystemVO.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .seq(entity.getSeq())
            .menus(menus)
            .build();
        list.add(build);
    }

    private List<Tree<Long>> listBaseTreeChecked(List<BaseTreeCheckedVO> allList, Long rootId) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("seq");
        return TreeUtil.build(allList, rootId, config, (object, tree) -> {
            tree.setId(object.getId());
            tree.setParentId(object.getParentId());
            tree.setWeight(object.getSeq());
            tree.setName(object.getName());
            tree.putExtra("level", object.getLevel());
            Optional.ofNullable(object.getType()).filter(CharSequenceUtil::isNotBlank)
                .ifPresent(val -> tree.putExtra("type", val));
            Optional.ofNullable(object.getChecked())
                .ifPresent(val -> tree.putExtra("checked", val));
        });
    }

    private List<Tree<Long>> listTree(List<MenuTreeVO> allList, Long rootId) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("seq");
        return TreeUtil.build(allList, rootId, config, (object, tree) -> {
            tree.setId(object.getId());
            tree.setParentId(object.getParentId());
            tree.setWeight(object.getSeq());
            tree.setName(object.getName());
            tree.putExtra("level", object.getLevel());
            Optional.ofNullable(object.getIconUrlStr()).filter(CharSequenceUtil::isNotBlank)
                .ifPresent(val -> tree.putExtra("iconUrlStr", val));
            Optional.ofNullable(object.getIconStyle()).filter(CharSequenceUtil::isNotBlank)
                .ifPresent(val -> tree.putExtra("iconStyle", val));
            tree.putExtra("routeUrl", object.getRouteUrl());
        });
    }

    private List<BaseTreeCheckedVO> listWithChecked(List<BaseTreeCheckedVO> allList, List<BaseTreeCheckedVO> checkedList) {
        return allList.stream().peek(map -> checkedList.stream()
                .filter(m -> Objects.equals(m.getId(), map.getId())).forEach(m -> map.setChecked(m.getChecked())))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
