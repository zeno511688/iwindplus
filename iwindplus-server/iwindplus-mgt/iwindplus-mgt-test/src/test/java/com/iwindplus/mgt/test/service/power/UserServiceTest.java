/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.test.service.power;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.dto.power.OrgSaveUserDTO;
import com.iwindplus.mgt.domain.dto.power.UserSaveEditDTO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import com.iwindplus.mgt.server.dal.mapper.power.UserGroupUserMapper;
import com.iwindplus.mgt.server.dal.mapper.power.UserOrgMapper;
import com.iwindplus.mgt.server.dal.mapper.power.UserPositionMapper;
import com.iwindplus.mgt.server.dal.mapper.power.UserRoleMapper;
import com.iwindplus.mgt.server.dal.model.power.UserDO;
import com.iwindplus.mgt.server.dal.repository.power.PositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserDepartmentRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserOrgRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserPositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserRoleRepository;
import com.iwindplus.mgt.server.service.power.impl.UserServiceImpl;
import com.iwindplus.mgt.test.BaseUnitTest;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户业务层测试.
 *
 * @author zengdegui
 * @since 2025/10/25 16:13
 */
public class UserServiceTest extends BaseUnitTest {

    @Mock
    private RedissonService redissonService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserOrgRepository userOrgRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UserPositionRepository userPositionRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private UserOrgMapper userOrgMapper;
    @Mock
    private UserPositionMapper userPositionMapper;
    @Mock
    private UserGroupUserMapper userGroupUserMapper;
    @Mock
    private UserDepartmentRepository userDepartmentRepository;
    @Mock
    private PositionRepository positionRepository;

    @Mock
    private RedissonSerialNumOperation redissonSerialNumOperation;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testSave() {
        // given
        UserSaveEditDTO dto = new UserSaveEditDTO();
        dto.setOrgId(10L);
        dto.setUsername("zengdegui");
        dto.setMobile("13812345678");
        dto.setPassword("123456");
        dto.setPositionIds(Set.of(3L));

        doAnswer(inv -> {
            UserDO u = inv.getArgument(0);
            u.setId(100L);
            return true;
        }).when(userRepository).save(any(UserDO.class));
        when(roleRepository.listDefaultRoles(10L)).thenReturn(Set.of(4L, 5L));
        when(passwordEncoder.encode(any())).thenReturn("encodedPwd");
        when(redissonService.serialNum()).thenReturn(redissonSerialNumOperation);
        when(redissonService.serialNum().getSerialNum(anyString(), anyInt(), anyBoolean()))
            .thenReturn("U000001");
        // Mock positionRepository 返回部门ID集合
        when(positionRepository.getDepartmentIdsByPositionIds(Set.of(3L)))
            .thenReturn(Set.of(5L));

        // when
        boolean result = userService.save(dto);

        // then
        assertThat(result).isTrue();
        assertThat(dto.getId()).isEqualTo(100L);

        verify(userRepository).save(any(UserDO.class));
        verify(userOrgRepository).saveBatchUser(10L, Set.of(100L));
        verify(userRoleRepository).saveBatchRole(100L, Set.of(4L, 5L));
        verify(userPositionRepository).saveBatchPosition(100L, Set.of(3L));
    }

    @Test
    void saveOrgUser() {
        Long userId = 1L;
        Long orgId = 10L;
        Set<Long> deptIds = Set.of(101L, 102L);
        Set<Long> posIds = Set.of(201L);

        OrgSaveUserDTO dto = new OrgSaveUserDTO();
        dto.setUserId(userId);
        dto.setOrgId(orgId);
        dto.setPositionIds(posIds);

        // 桩数据
        Set<Long> defaultRoles = Set.of(30L, 31L);
        when(roleRepository.listDefaultRoles(orgId)).thenReturn(defaultRoles);
        // Mock positionRepository 返回部门ID集合
        when(positionRepository.getDepartmentIdsByPositionIds(posIds))
            .thenReturn(Set.of(50L));

        // 执行
        boolean result = userService.saveOrgUser(dto);

        // 断言
        assertTrue(result);

        // 验证交互
        verify(userRepository).getUserIdIsNotExist(userId);

        verify(userOrgRepository).saveBatchUser(orgId, Set.of(userId));

        verify(roleRepository).listDefaultRoles(orgId);

        verify(userRoleRepository).saveBatchRole(userId, defaultRoles);

        verify(userPositionRepository).saveBatchPosition(userId, posIds);
    }

    @Test
    void testGetDetail() {
        // given
        Long id = 1543960516026822658L;
        UserDO mockDO = new UserDO();
        mockDO.setId(id);
        mockDO.setUsername("mock");

        // 【关键】打桩 Repository，而不是 Mapper
        when(userRepository.getById(id)).thenReturn(mockDO);

        // when
        UserVO actual = userService.getDetail(id);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getUsername()).isEqualTo("mock");

        // 验证 Repository 被调用过一次（可选）
        verify(userRepository).getById(id);

        Long id2 = 999L;
        when(userRepository.getById(id2)).thenReturn(null);

        BizException thrown = assertThrows(BizException.class,
            () -> userService.getDetail(id2));

        assertThat(thrown.getBizCode()).isEqualTo(BizCodeEnum.DATA_NOT_EXIST.getBizCode());
    }
}
