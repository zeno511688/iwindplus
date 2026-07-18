/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseTreeCheckedVO;
import com.iwindplus.base.domain.vo.BaseTreeVO;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.MenuEditDTO;
import com.iwindplus.mgt.domain.dto.power.MenuSaveDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.power.MenuBaseListSystemVO;
import com.iwindplus.mgt.domain.vo.power.MenuExtendVO;
import com.iwindplus.mgt.domain.vo.power.MenuListSystemVO;
import com.iwindplus.mgt.domain.vo.power.MenuTreeSystemVO;
import com.iwindplus.mgt.domain.vo.power.MenuTreeVO;
import com.iwindplus.mgt.domain.vo.power.MenuVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseCheckedVO;
import com.iwindplus.mgt.server.config.property.MgtProperty;
import com.iwindplus.mgt.server.dal.model.power.MenuDO;
import com.iwindplus.mgt.server.dal.model.power.ResourceDO;
import com.iwindplus.mgt.server.dal.repository.power.MenuRepository;
import com.iwindplus.mgt.server.dal.repository.power.ResourceRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleMenuRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleResourceRepository;
import com.iwindplus.mgt.server.service.power.MenuService;
import com.iwindplus.setup.client.OssClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资源业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MENU})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final ResourceRepository resourceRepository;
    private final OssClient ossClient;
    private final RedissonService redissonService;
    private final MgtProperty property;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(MenuSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.menuRepository.getNameIsExist(entity.getName().trim(), entity.getSystemId(), entity.getParentId());
        entity.setSeq(this.menuRepository.getNextSeq(entity.getSystemId(), entity.getParentId()));
        entity.setLevel(this.menuRepository.getLevel(entity.getSystemId(), entity.getParentId()));
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.MENU_PREFIX.getValue()));
        } else {
            if (!entity.getCode().startsWith(MgtCodePrefixEnum.MENU_PREFIX.getValue())) {
                throw new BizException(MgtCodeEnum.MENU_PREFIX_ERROR);
            }
        }
        this.menuRepository.getCodeIsExist(entity.getCode());
        if (CharSequenceUtil.isNotBlank(entity.getRouteUrl())) {
            this.menuRepository.getRouteUrlIsExist(entity.getRouteUrl().trim(), entity.getSystemId(), entity.getParentId());
        }
        final MenuDO model = BeanUtil.copyProperties(entity, MenuDO.class);
        this.menuRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<MenuDO> list = this.menuRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(MenuDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        // 判断是否有子集
        boolean data = SqlHelper.retBool(this.menuRepository.count(Wrappers.lambdaQuery(MenuDO.class)
            .in(MenuDO::getParentId, ids)));
        if (data) {
            throw new BizException(MgtCodeEnum.CHILDREN_NOT_DELETED);
        }
        // 判断是否有资源
        final boolean hasResource = SqlHelper.retBool(this.resourceRepository.count(Wrappers.lambdaQuery(ResourceDO.class)
            .in(ResourceDO::getMenuId, ids)));
        if (Boolean.TRUE.equals(hasResource)) {
            throw new BizException(MgtCodeEnum.RESOURCE_NOT_DELETED);
        }
        this.roleMenuRepository.getBaseMapper().deleteByMenuIds(ids);
        this.roleResourceRepository.getBaseMapper().deleteByMenuIds(ids);
        this.menuRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(MenuEditDTO entity) {
        MenuDO data = this.menuRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.menuRepository.getNameIsExist(entity.getName().trim(), data.getSystemId(), data.getParentId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        entity.setLevel(this.menuRepository.getLevel(data.getSystemId(), entity.getParentId()));
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.menuRepository.getCodeIsExist(entity.getCode());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRouteUrl()) && !CharSequenceUtil.equals(data.getRouteUrl(), entity.getRouteUrl().trim())) {
            this.menuRepository.getRouteUrlIsExist(entity.getRouteUrl().trim(), data.getSystemId(), data.getParentId());
        }
        final MenuDO model = BeanUtil.copyProperties(entity, MenuDO.class);
        this.menuRepository.updateById(model);
        this.removeOldPic(entity, data);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        MenuDO data = this.menuRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MenuDO param = new MenuDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.menuRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        MenuDO data = this.menuRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MenuDO param = new MenuDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.menuRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<Tree<Long>> listBySystemId(Long systemId) {
        return this.listBySystemId(systemId, null);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<Tree<Long>> listEnabledBySystemId(Long systemId) {
        return this.listBySystemId(systemId, EnableStatusEnum.ENABLE);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    @Override
    public List<MenuTreeSystemVO> listByUserId(Long orgId, Long userId) {
        List<MenuTreeSystemVO> result = new ArrayList<>(16);
        final List<MenuListSystemVO> list = this.menuRepository.getBaseMapper().selectListByUserId(orgId, userId);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        list.stream().filter(Objects::nonNull).forEach(data -> this.buildMenuListSystem(result, data, this.listTree(data.getMenus(), 0L)));
        Collections.sort(result, Comparator.comparing(MenuTreeSystemVO::getSeq));
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    @Override
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
        Collections.sort(result, Comparator.comparing(MenuTreeSystemVO::getSeq));
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MenuVO getDetail(Long id) {
        MenuDO data = this.menuRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, MenuVO.class);
    }

    @Override
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
        List<FilePathVO> filePaths = OrgServiceImpl.getFilePaths(this.property.getOss().getTplCode(), relativePaths, this.ossClient);
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
        result.stream().forEach(data -> this.buildMenuBaseListSystem(list, data, this.listBaseTreeChecked(data.getMenus(), 0L)));
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
            Optional.ofNullable(object.getChecked()).filter(Objects::nonNull)
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

    private void removeOldPic(MenuEditDTO entity, MenuDO data) {
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(entity.getIconUrl())
            && CharSequenceUtil.isNotBlank(data.getIconUrl())
            && !CharSequenceUtil.equals(data.getIconUrl(), entity.getIconUrl().trim())) {
            relativePaths.add(data.getIconUrl());
        }
        if (CollUtil.isNotEmpty(relativePaths)) {
            OrgServiceImpl.removeFiles(this.ossClient, this.property.getOss().getTplCode(), relativePaths);
        }
    }
}
