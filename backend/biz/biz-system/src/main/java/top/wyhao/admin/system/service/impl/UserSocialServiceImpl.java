/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.model.enums.SocialSource;
import top.wyhao.admin.system.mapper.user.UserSocialMapper;
import top.wyhao.admin.system.model.entity.user.UserSocialDO;
import top.wyhao.admin.system.service.UserSocialService;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.validation.BizAssert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户社交账号服务类
 *
 * @author wyhao
 * @since 2025/12/7
 */
@Service
@RequiredArgsConstructor
public class UserSocialServiceImpl implements UserSocialService {

    private final UserSocialMapper baseMapper;

    @Override
    public UserSocialDO getBySourceAndOpenId(String source, String openId) {
        return baseMapper.selectBySourceAndOpenId(source, openId);
    }

    @Override
    public void saveOrUpdate(UserSocialDO userSocial) {
        if (userSocial.getCreateTime() == null) {
            baseMapper.insert(userSocial);
        } else {
            baseMapper.lambdaUpdate()
                .set(UserSocialDO::getMetaJson, userSocial.getMetaJson())
                .set(UserSocialDO::getLastLoginTime, userSocial.getLastLoginTime())
                .eq(UserSocialDO::getSource, userSocial.getSource())
                .eq(UserSocialDO::getOpenId, userSocial.getOpenId())
                .update();
        }
    }

    @Override
    public List<UserSocialDO> listByUserId(Long userId) {
        return baseMapper.lambdaQuery().eq(UserSocialDO::getUserId, userId).list();
    }

    @Override
    public void bind(AuthUser authUser, Long userId) {
        String source = authUser.getSource();
        String openId = authUser.getUuid();
        List<UserSocialDO> userSocialList = this.listByUserId(userId);
        Set<String> boundSocialSet = CollUtils.mapToSet(userSocialList, UserSocialDO::getSource);
        String description = SocialSource.valueOf(source).getDescription();
        BizAssert.isTrue(boundSocialSet.contains(source), "您已经绑定过了 [{}] 平台，请先解绑", description);
        UserSocialDO userSocial = this.getBySourceAndOpenId(source, openId);
        BizAssert.throwIfNotNull(userSocial, "[{}] 平台账号 [{}] 已被其他用户绑定", description, authUser.getUsername());
        userSocial = new UserSocialDO();
        userSocial.setUserId(userId);
        userSocial.setSource(source);
        userSocial.setOpenId(openId);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now());
        baseMapper.insert(userSocial);
    }

    @Override
    public void deleteBySourceAndUserId(String source, Long userId) {
        baseMapper.lambdaUpdate().eq(UserSocialDO::getSource, source).eq(UserSocialDO::getUserId, userId).remove();
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(UserSocialDO::getUserId, userIds).remove();
    }
}
