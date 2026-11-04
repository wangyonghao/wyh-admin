package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.system.admin.app.service.UserSocialService;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.SocialOAuthGateway;
import top.wyhao.system.admin.domain.gateway.UserSocialRepository;
import top.wyhao.system.admin.domain.model.SocialIdentity;
import top.wyhao.system.admin.domain.model.SocialSource;
import top.wyhao.system.admin.domain.model.UserSocial;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户社交账号服务类
 */
@Service
@RequiredArgsConstructor
public class UserSocialServiceImpl implements UserSocialService {

    private final UserSocialRepository userSocialRepository;
    private final SocialOAuthGateway socialOAuthGateway;

    @Override
    public UserSocial getBySourceAndOpenId(String source, String openId) {
        return userSocialRepository.findBySourceAndOpenId(source, openId);
    }

    @Override
    public void saveOrUpdate(UserSocial userSocial) {
        if (userSocial.getCreateTime() == null) {
            userSocialRepository.insert(userSocial);
        } else {
            userSocialRepository.updateLoginMeta(userSocial);
        }
    }

    @Override
    public List<UserSocial> listByUserId(Long userId) {
        return userSocialRepository.listByUserId(userId);
    }

    @Override
    public void bindFromOAuth(String source, String code, String state, Long userId) {
        SocialIdentity identity = socialOAuthGateway.authenticate(source, code, state);
        bind(identity, userId);
    }

    @Override
    public void bind(SocialIdentity identity, Long userId) {
        String source = identity.source();
        String openId = identity.openId();
        List<UserSocial> userSocialList = this.listByUserId(userId);
        Set<String> boundSocialSet = CollUtils.mapToSet(userSocialList, UserSocial::getSource);
        String description = SocialSource.valueOf(source).getDescription();
        if (boundSocialSet.contains(source)) {
            throw UserException.socialAlreadyBound(description);
        }
        UserSocial userSocial = this.getBySourceAndOpenId(source, openId);
        if (userSocial != null) {
            throw UserException.socialBoundByOther(description, identity.username());
        }
        userSocial = new UserSocial();
        userSocial.setUserId(userId);
        userSocial.setSource(source);
        userSocial.setOpenId(openId);
        userSocial.setMetaJson(identity.metaJson());
        userSocial.setLastLoginTime(LocalDateTime.now());
        userSocialRepository.insert(userSocial);
    }

    @Override
    public void deleteBySourceAndUserId(String source, Long userId) {
        userSocialRepository.deleteBySourceAndUserId(source, userId);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        userSocialRepository.deleteByUserIds(userIds);
    }
}
