/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.test.service.upms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgSaveUserDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSaveEditDTO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupUserMapper;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserOrgMapper;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserPositionMapper;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRoleMapper;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.PositionRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserDepartmentRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserOrgRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserPositionRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRoleRepository;
import com.iwindplus.mgt.application.service.upms.user.UserApplicationService;
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
    private RedissonExecutor redissonApplicationService;
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
    private UserApplicationService userApplicationService;

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
        when(redissonApplicationService.serialNum()).thenReturn(redissonSerialNumOperation);
        when(redissonApplicationService.serialNum().getSerialNum(anyString(), anyInt(), anyBoolean()))
            .thenReturn("U000001");
        // Mock positionRepository 返回部门ID集合
        when(positionRepository.getDepartmentIdsByPositionIds(Set.of(3L)))
            .thenReturn(Set.of(5L));

        // when
        boolean result = userApplicationService.save(dto);

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
        boolean result = userApplicationService.saveOrgUser(dto);

        // 断言
        assertTrue(result);

        // 验证交互
        verify(userRepository).getUserIdIsNotExist(userId);

        verify(userOrgRepository).saveBatchUser(orgId, Set.of(userId));

        verify(roleRepository).listDefaultRoles(orgId);

        verify(userRoleRepository).saveBatchRole(userId, defaultRoles);

        verify(userPositionRepository).saveBatchPosition(userId, posIds);
    }
}
