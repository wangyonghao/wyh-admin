package top.wyhao.system.admin.adapter.client;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.system.admin.app.service.UserAdminService;
import top.wyhao.system.admin.app.service.UserProfileService;
import top.wyhao.system.admin.app.service.UserSocialService;
import top.wyhao.identity.client.UserCredential;
import top.wyhao.identity.client.SocialLink;
import top.wyhao.identity.client.UserApi;
import top.wyhao.identity.client.UserProfile;
import top.wyhao.identity.client.UserProfileApi;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.domain.model.UserSocial;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户身份 API 实现
 */
@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi, UserProfileApi {

    private final UserAdminService userAdminService;
    private final UserProfileService userProfileService;
    private final UserSocialService userSocialService;

    @Override
    public long countByDepartmentIds(Collection<Long> departmentIds) {
        return userAdminService.countByDepartmentIds(List.copyOf(departmentIds));
    }

    @Override
    public UserCredential findById(Long id) {
        return toCredential(userAdminService.getById(id));
    }

    @Override
    public UserCredential findByUsername(String username) {
        return toCredential(userAdminService.getByUsername(username));
    }

    @Override
    public UserCredential findByPhone(String phone) {
        return toCredential(userAdminService.getByPhone(phone));
    }

    @Override
    public UserCredential findByEmail(String email) {
        return toCredential(userAdminService.getByEmail(email));
    }

    @Override
    public UserCredential registerSocialUser(String username, String displayName, Integer gender, Long departmentId) {
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setGender(gender);
        user.setDepartmentId(departmentId);
        user.setStatus(StatusEnum.ENABLE.getValue());
        userAdminService.save(user);
        return toCredential(user);
    }

    @Override
    public UserCredential registerLocalUser(String username, String rawPassword, Long departmentId, java.util.List<Long> roleIds) {
        return toCredential(userProfileService.registerLocal(username, rawPassword, departmentId, roleIds));
    }

    @Override
    public SocialLink findSocial(String source, String openId) {
        UserSocial social = userSocialService.getBySourceAndOpenId(source, openId);
        if (social == null) {
            return null;
        }
        return new SocialLink(social.getUserId(), social.getSource(), social.getOpenId());
    }

    @Override
    public void saveSocialLogin(Long userId, String source, String openId, String metaJson) {
        UserSocial social = userSocialService.getBySourceAndOpenId(source, openId);
        if (social == null) {
            social = new UserSocial();
        }
        social.setUserId(userId);
        social.setSource(source);
        social.setOpenId(openId);
        social.setMetaJson(metaJson);
        social.setLastLoginTime(LocalDateTime.now());
        userSocialService.saveOrUpdate(social);
    }

    @Override
    public UserProfile profile(Long id) {
        UserProfile profile = new UserProfile();
        BeanUtil.copyProperties(userAdminService.detail(id), profile);
        return profile;
    }

    private UserCredential toCredential(User user) {
        if (user == null) {
            return null;
        }
        return new UserCredential(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPassword(),
                user.getStatus(),
                user.getGender(),
                user.getDepartmentId(),
                user.getPwdUpdateTime(),
                user.getPwdExpireDate()
        );
    }
}
