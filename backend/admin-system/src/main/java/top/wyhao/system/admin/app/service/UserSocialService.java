package top.wyhao.system.admin.app.service;

import top.wyhao.system.admin.domain.model.SocialIdentity;
import top.wyhao.system.admin.domain.model.UserSocial;

import java.util.List;

/**
 * 用户社会化关联业务接口
 */
public interface UserSocialService {

    UserSocial getBySourceAndOpenId(String source, String openId);

    void saveOrUpdate(UserSocial userSocial);

    List<UserSocial> listByUserId(Long userId);

    /**
     * OAuth 回调绑定：经 gateway 换身份后落库。
     */
    void bindFromOAuth(String source, String code, String state, Long userId);

    /**
     * 绑定（入参为自有 DTO，不含 JustAuth）。
     */
    void bind(SocialIdentity identity, Long userId);

    void deleteBySourceAndUserId(String source, Long userId);

    void deleteByUserIds(List<Long> userIds);
}
