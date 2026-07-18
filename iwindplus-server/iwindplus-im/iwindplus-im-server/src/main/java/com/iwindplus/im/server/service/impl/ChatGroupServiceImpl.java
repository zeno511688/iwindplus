/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.util.ImageUtil;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.im.domain.constant.ImConstant;
import com.iwindplus.im.domain.dto.ChatGroupEditDTO;
import com.iwindplus.im.domain.dto.ChatGroupJoinDTO;
import com.iwindplus.im.domain.dto.ChatGroupSaveDTO;
import com.iwindplus.im.domain.dto.ChatGroupSearchDTO;
import com.iwindplus.im.domain.dto.ChatGroupUserSaveDTO;
import com.iwindplus.im.domain.enums.ChatGroupStatusEnum;
import com.iwindplus.im.domain.enums.ImCodeEnum;
import com.iwindplus.im.domain.enums.JoinTypeEnum;
import com.iwindplus.im.domain.vo.ChatGroupBaseVO;
import com.iwindplus.im.domain.vo.ChatGroupPageVO;
import com.iwindplus.im.domain.vo.ChatGroupVO;
import com.iwindplus.im.server.config.property.ImProperty;
import com.iwindplus.im.server.dal.model.ChatGroupDO;
import com.iwindplus.im.server.dal.model.ChatGroupUserDO;
import com.iwindplus.im.server.dal.repository.ChatGroupRepository;
import com.iwindplus.im.server.dal.repository.ChatGroupUserRepository;
import com.iwindplus.im.server.service.ChatGroupService;
import com.iwindplus.im.server.service.GroupChatMsgService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.setup.client.OssClient;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聊天群业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ChatGroupServiceImpl implements ChatGroupService {

    private final UserClient userClient;
    private final OssClient ossClient;
    private final ChatGroupRepository chatGroupRepository;
    private final ChatGroupUserRepository chatGroupUserRepository;
    private final GroupChatMsgService groupChatMsgService;
    private final ImProperty imProperty;

    @Override
    public ChatGroupVO saveChatGroup(ChatGroupSaveDTO entity) {
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        // 拉取用户不能超过30人
        if (entity.getUserIds().size() > CommonConstant.NumberConstant.NUMBER_THIRTY) {
            throw new BizException(ImCodeEnum.PULL_USER_CANNOT_EXCEED);
        }
        entity.setOrgId(orgId);
        entity.setStatus(ChatGroupStatusEnum.NORMAL);
        entity.setShowNickNameFlag(Boolean.TRUE);
        entity.setLimitNum(CommonConstant.NumberConstant.NUMBER_FIVE_HUNDRED);
        final List<Long> ids = new LinkedList<>();
        ids.add(userId);
        ids.addAll(entity.getUserIds());
        final List<UserExtendVO> userList = Optional.ofNullable(this.userClient.listExtendByIds(ids))
            .map(ResultVO::getBizData).orElse(Lists.newArrayList());
        if (CollUtil.isEmpty(userList)) {
            return null;
        }
        // 群名称
        this.buildGroupName(entity, userList);
        // 群头像（前九个的头像拼接为群头像）
        String groupAvatarAbsolutePath = this.buildGroupAvatar(entity, userList);
        final ChatGroupDO model = BeanUtil.copyProperties(entity, ChatGroupDO.class);
        this.chatGroupRepository.save(model);
        // 群二维码
        String groupQrcodeAbsolutePath = this.buildGroupQrcode(model);
        this.chatGroupRepository.update(Wrappers.lambdaUpdate(ChatGroupDO.class)
            .set(ChatGroupDO::getGroupQrcode, model.getGroupQrcode())
            .eq(ChatGroupDO::getId, model.getId()));
        final List<ChatGroupUserSaveDTO> entities = new ArrayList<>(10);
        userList.stream().filter(Objects::nonNull).forEach(data -> {
            ChatGroupUserSaveDTO param = new ChatGroupUserSaveDTO();
            param.setNickName(data.getNickName());
            param.setLeaderFlag(data.getId().equals(userId));
            param.setJoinType(JoinTypeEnum.INVITE);
            param.setAgreeFlag(Boolean.TRUE);
            param.setChatGroupId(model.getId());
            param.setUserId(data.getId());
            param.setUserAvatar(data.getAvatar());
            param.setUserNickName(data.getNickName());
            param.setRemark(data.getId().equals(userId) ? ImConstant.GROUP_LEADER : ImConstant.MEMBER);
            param.setCurrentUserId(userId);
            param.setOrgId(orgId);
            param.setOrgId(orgId);
            entities.add(param);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.chatGroupUserRepository.saveBatch(entities);
        }
        model.setGroupAvatar(groupAvatarAbsolutePath);
        model.setGroupQrcode(groupQrcodeAbsolutePath);
        return BeanUtil.copyProperties(model, ChatGroupVO.class);
    }

    @Override
    public boolean saveJoinChatGroup(ChatGroupJoinDTO entity) {
        final ChatGroupDO chatGroup = this.chatGroupRepository.getById(entity.getChatGroupId());
        if (Objects.isNull(chatGroup)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final long size = this.chatGroupUserRepository.count(Wrappers.lambdaQuery(ChatGroupUserDO.class)
            .eq(ChatGroupUserDO::getChatGroupId, entity.getChatGroupId()));
        // 群组人数不能超过500人
        if (size + entity.getUserIds().size() > CommonConstant.NumberConstant.NUMBER_FIVE_HUNDRED) {
            throw new BizException(ImCodeEnum.GROUP_USER_CANNOT_EXCEED);
        }
        // 拉取用户不能超过30人
        if (entity.getUserIds().size() > CommonConstant.NumberConstant.NUMBER_THIRTY) {
            throw new BizException(ImCodeEnum.PULL_USER_CANNOT_EXCEED);
        }
        List<Long> userIds = entity.getUserIds().stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
        final List<UserExtendVO> userList = Optional.ofNullable(this.userClient.listExtendByIds(userIds))
            .map(ResultVO::getBizData).orElse(Lists.newArrayList());
        if (CollUtil.isEmpty(userList)) {
            return false;
        }
        final Set<ChatGroupUserDO> entities = new LinkedHashSet<>(16);
        userList.forEach(data -> {
            final ChatGroupUserDO param = ChatGroupUserDO.builder()
                .nickName(data.getNickName())
                .leaderFlag(Boolean.FALSE)
                .joinType(entity.getJoinType())
                .agreeFlag(Boolean.TRUE)
                .chatGroupId(entity.getChatGroupId())
                .userId(data.getId())
                .userAvatar(data.getAvatar())
                .userNickName(data.getNickName())
                .build();
            param.setRemark(ImConstant.MEMBER);
            entities.add(param);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.chatGroupUserRepository.saveBatch(entities, 1000);
            this.editGroupAvatarPath(entity, chatGroup);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean removeChatGroup(Long id, Long currentUserId) {
        final List<Long> ids = Stream.of(id).collect(Collectors.toCollection(ArrayList::new));
        final ChatGroupDO data = this.chatGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        // 判断为空群则解散群
        final long count = this.chatGroupUserRepository.count(Wrappers.lambdaQuery(ChatGroupUserDO.class).eq(ChatGroupUserDO::getChatGroupId, id));
        if (count == 0) {
            return SqlHelper.retBool(this.chatGroupRepository.getBaseMapper().deleteByIds(ids));
        }
        final ChatGroupUserDO chatGroupUser = this.chatGroupUserRepository.getOne(
            Wrappers.lambdaQuery(ChatGroupUserDO.class).eq(ChatGroupUserDO::getUserId, currentUserId).eq(ChatGroupUserDO::getChatGroupId, id));
        if (Objects.isNull(chatGroupUser)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        // 当前用户是群主则解散群
        if (Boolean.TRUE.equals(chatGroupUser.getLeaderFlag())) {
            this.chatGroupUserRepository.getBaseMapper().deleteByChatGroupIds(ids);
            this.groupChatMsgService.removeByChatGroupIds(ids);
            return SqlHelper.retBool(this.chatGroupRepository.getBaseMapper().deleteByIds(ids));
        }
        // 退出群
        this.chatGroupUserRepository.getBaseMapper()
            .deleteByChatGroupIds(Stream.of(chatGroupUser.getId()).collect(Collectors.toCollection(ArrayList::new)));
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(ChatGroupEditDTO entity) {
        Long orgId = entity.getOrgId();
        final ChatGroupDO data = this.chatGroupRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        // 校验群名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getGroupName()) && !CharSequenceUtil.equals(data.getGroupName(), entity.getGroupName().trim())) {
            this.getGroupNameIsExist(entity.getGroupName().trim(), orgId);
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final ChatGroupDO model = BeanUtil.copyProperties(entity, ChatGroupDO.class);
        this.chatGroupRepository.updateById(model);
        this.removeOldPic(entity, data);
        return Boolean.TRUE;
    }

    @Override
    public IPage<ChatGroupPageVO> page(PageDTO<ChatGroupDO> page, ChatGroupSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(CommonConstant.DbConstant.MODIFIED_TIME);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = "cg." + order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        final IPage<ChatGroupPageVO> modelPage = this.chatGroupRepository.getBaseMapper().selectPageByCondition(page, entity);
        return modelPage;
    }

    @Override
    public ChatGroupVO getDetail(Long id, String ossTplCode) {
        ChatGroupDO data = this.chatGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getGroupAvatar())) {
            relativePaths.add(data.getGroupAvatar());
        }
        if (CharSequenceUtil.isNotBlank(data.getGroupQrcode())) {
            relativePaths.add(data.getGroupQrcode());
        }
        List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
        final ChatGroupVO result = BeanUtil.copyProperties(data, ChatGroupVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    @Override
    public List<Long> listByUserId(Long userId, Long orgId) {
        return this.chatGroupRepository.getBaseMapper().selectByUserId(userId, orgId);
    }

    @Override
    public List<ChatGroupBaseVO> listByOrgId(Long orgId) {
        final List<ChatGroupDO> list = this.chatGroupRepository.list(Wrappers.lambdaQuery(ChatGroupDO.class)
            .eq(ChatGroupDO::getOrgId, orgId).select(ChatGroupDO::getId, ChatGroupDO::getGroupName));
        if (CollUtil.isNotEmpty(list)) {
            return BeanUtil.copyToList(list, ChatGroupBaseVO.class);
        }
        return null;
    }

    /**
     * 检测群名称是否存在.
     *
     * @param groupName 群名称
     * @param orgId     组织主键
     */
    private void getGroupNameIsExist(String groupName, Long orgId) {
        LambdaQueryWrapper<ChatGroupDO> queryWrapper = Wrappers.lambdaQuery(ChatGroupDO.class)
            .eq(ChatGroupDO::getGroupName, groupName)
            .eq(ChatGroupDO::getOrgId, orgId);
        boolean result = SqlHelper.retBool(this.chatGroupRepository.count(queryWrapper));
        if (result) {
            throw new BizException(ImCodeEnum.GROUP_NAME_EXIST);
        }
    }

    private void buildGroupName(ChatGroupSaveDTO entity, List<UserExtendVO> userList) {
        if (CollUtil.isEmpty(userList)) {
            return;
        }
        List<String> nickNames;
        if (userList.size() >= CommonConstant.NumberConstant.NUMBER_THREE) {
            nickNames = userList.subList(0, CommonConstant.NumberConstant.NUMBER_THREE).stream()
                .filter(Objects::nonNull).map(UserExtendVO::getNickName)
                .collect(Collectors.toCollection(ArrayList::new));
        } else {
            nickNames = userList.stream().filter(Objects::nonNull)
                .map(UserExtendVO::getNickName).collect(Collectors.toCollection(ArrayList::new));
        }
        if (CollUtil.isEmpty(nickNames)) {
            return;
        }
        String groupName = CharSequenceUtil.join(CommonConstant.SymbolConstant.COMMA, nickNames);
        entity.setGroupName(groupName);
        entity.setRemark(groupName);
    }

    private String buildGroupAvatar(ChatGroupSaveDTO entity, List<UserExtendVO> userList) {
        if (CollUtil.isEmpty(userList)) {
            return null;
        }
        List<String> avatarUrls;
        if (userList.size() >= CommonConstant.NumberConstant.NUMBER_NINE) {
            avatarUrls = userList.subList(0, CommonConstant.NumberConstant.NUMBER_NINE).stream()
                .filter(Objects::nonNull).map(UserExtendVO::getAvatarStr)
                .collect(Collectors.toCollection(ArrayList::new));
            entity.setEditAvatarFlag(Boolean.TRUE);
        } else {
            avatarUrls = userList.stream().filter(Objects::nonNull)
                .map(UserExtendVO::getAvatarStr).collect(Collectors.toCollection(ArrayList::new));
        }
        if (CollUtil.isEmpty(avatarUrls)) {
            return null;
        }
        byte[] avatarByte = ImageUtil.generate(avatarUrls, FileTypeEnum.PNG.getType());
        if (PrimitiveArrayUtil.isEmpty(avatarByte)) {
            return null;
        }
        UploadVO uploadVO = this.getUploadVO(avatarByte);
        entity.setGroupAvatar(Optional.ofNullable(uploadVO).map(UploadVO::getRelativePath).orElse(null));
        return Optional.ofNullable(uploadVO).map(UploadVO::getAbsolutePath).orElse(null);
    }

    private void editGroupAvatarPath(ChatGroupJoinDTO entity, ChatGroupDO chatGroup) {
        // 判断是否禁止修改群头像
        if (Boolean.TRUE.equals(chatGroup.getEditAvatarFlag())) {
            return;
        }
        final LambdaQueryWrapper<ChatGroupUserDO> queryWrapper = Wrappers.lambdaQuery(ChatGroupUserDO.class)
            .eq(ChatGroupUserDO::getChatGroupId, entity.getChatGroupId())
            .select(ChatGroupUserDO::getUserId)
            .orderByAsc(ChatGroupUserDO::getSeq).last("limit 0, 9");
        Function<Object, Long> function = val -> Long.valueOf(val.toString());
        List<Long> ids = this.chatGroupUserRepository.listObjs(queryWrapper, function);
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<UserExtendVO> userList = Optional.ofNullable(this.userClient.listExtendByIds(ids))
            .map(ResultVO::getBizData).orElse(Lists.newArrayList());
        if (CollUtil.isEmpty(userList)) {
            return;
        }
        final List<String> avatarUrls = userList.stream()
            .filter(Objects::nonNull).map(UserExtendVO::getAvatarStr).collect(Collectors.toCollection(ArrayList::new));
        if (CollUtil.isEmpty(avatarUrls)) {
            return;
        }
        byte[] avatarByte = ImageUtil.generate(avatarUrls, FileTypeEnum.PNG.getType());
        if (PrimitiveArrayUtil.isEmpty(avatarByte)) {
            return;
        }
        final String relativePath = Optional.ofNullable(this.getUploadVO(avatarByte))
            .map(UploadVO::getRelativePath).orElse(null);
        if (CharSequenceUtil.isBlank(relativePath)) {
            return;
        }
        this.editGroupAvatar(chatGroup, ids, relativePath);
        // 删除oss原来的图片
        this.ossClient.removeFiles(this.imProperty.getOss().getTplCode(), Arrays.asList(chatGroup.getGroupAvatar()));
    }

    private void editGroupAvatar(ChatGroupDO data, List<Long> ids, String relativePath) {
        ChatGroupDO param = new ChatGroupDO();
        param.setId(data.getId());
        param.setGroupAvatar(relativePath);
        param.setVersion(data.getVersion());
        // 图片等于9设置禁止修改群头像
        if (ids.size() == CommonConstant.NumberConstant.NUMBER_NINE) {
            param.setEditAvatarFlag(Boolean.TRUE);
        }
        this.chatGroupRepository.updateById(param);
    }

    private String buildGroupQrcode(ChatGroupDO entity) {
        String content = UrlBuilder.ofHttp(this.imProperty.getChatGroupScanUrl())
            .addQuery(ImConstant.CHAT_GROUP_ID, entity.getId())
            .build();
        byte[] qrCodeByte = QrCodeUtil.generatePng(content, 150, 150);
        if (PrimitiveArrayUtil.isEmpty(qrCodeByte)) {
            return null;
        }
        UploadVO uploadVO = this.getUploadVO(qrCodeByte);
        entity.setGroupQrcode(Optional.ofNullable(uploadVO).map(UploadVO::getRelativePath).orElse(null));
        return Optional.ofNullable(uploadVO).map(UploadVO::getAbsolutePath).orElse(null);
    }

    private UploadVO getUploadVO(byte[] data) {
        String sourceFileName = new StringBuilder(IdUtil.getSnowflakeNextIdStr()).append(CommonConstant.SymbolConstant.POINT)
            .append(FileTypeEnum.PNG.getType()).toString();
        final UploadByteDTO attachment = UploadByteDTO.builder()
            .data(ArrayUtil.wrap(data))
            .sourceFileName(FileUtil.getName(sourceFileName))
            .contentType(FileTypeEnum.PNG.getContentType())
            .build();
        OssUploadByteDTO avatarUpload = OssUploadByteDTO.builder()
            .attachment( attachment)
            .tplCode(this.imProperty.getOss().getTplCode()).prefix("pic/chatGroup")
            .renamed(Boolean.TRUE)
            .build();
        return Optional.ofNullable(this.ossClient.uploadByte(avatarUpload))
            .map(ResultVO::getBizData).orElse(null);
    }

    private void removeOldPic(ChatGroupEditDTO entity, ChatGroupDO data) {
        if (CharSequenceUtil.isNotBlank(entity.getOssTplCode())) {
            List<String> relativePaths = new ArrayList<>(10);
            if (CharSequenceUtil.isNotBlank(entity.getGroupAvatar()) && !CharSequenceUtil.equals(data.getGroupAvatar(),
                entity.getGroupAvatar().trim())) {
                relativePaths.add(data.getGroupAvatar());
            }
            if (CharSequenceUtil.isNotBlank(entity.getGroupQrcode()) && !CharSequenceUtil.equals(data.getGroupQrcode(),
                entity.getGroupQrcode().trim())) {
                relativePaths.add(data.getGroupQrcode());
            }
            if (CollUtil.isNotEmpty(relativePaths)) {
                this.ossClient.removeFiles(entity.getOssTplCode(), relativePaths);
            }
        }
    }

    private void buildUserInfo(ChatGroupVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getGroupAvatar()) && data.getGroupAvatar().equals(p.getRelativePath())) {
                data.setGroupAvatar(p.getAbsolutePath());
            }
            if (CharSequenceUtil.isNotBlank(data.getGroupQrcode()) && data.getGroupQrcode().equals(p.getRelativePath())) {
                data.setGroupQrcode(p.getAbsolutePath());
            }
        });
    }
}
