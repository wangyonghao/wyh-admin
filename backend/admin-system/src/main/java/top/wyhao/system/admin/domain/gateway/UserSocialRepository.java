package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.UserSocial;

import java.util.Collection;
import java.util.List;

/**
 * 用户第三方账号仓储。
 */
public interface UserSocialRepository {

    UserSocial findBySourceAndOpenId(String source, String openId);

    void insert(UserSocial social);

    void updateLoginMeta(UserSocial social);

    List<UserSocial> listByUserId(Long userId);

    void deleteBySourceAndUserId(String source, Long userId);

    void deleteByUserIds(List<Long> userIds);

    void deleteAll();

    void deleteExcludingUserIds(Collection<Long> keepUserIds);
}
