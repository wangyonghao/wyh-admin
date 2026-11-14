package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alicp.jetcache.anno.CacheUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.cmn.core.constant.CacheConstants;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.system.admin.app.service.FileService;
import top.wyhao.system.admin.domain.model.File;
import top.wyhao.system.admin.app.dto.UserBasicInfoUpdateReq;
import top.wyhao.system.admin.app.service.MembershipService;
import top.wyhao.system.admin.app.service.UserProfileService;
import top.wyhao.identity.client.EffectivePasswordRules;
import top.wyhao.identity.client.PasswordApi;
import top.wyhao.identity.client.PasswordPolicyConfig;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.PasswordPolicyGateway;
import top.wyhao.system.admin.domain.model.PasswordHistory;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.service.RegisterUsernameAllocator;
import top.wyhao.system.admin.domain.service.UniqueConflict;
import top.wyhao.system.admin.domain.service.UserLifecycleRules;
import top.wyhao.system.admin.domain.service.UserDomainService;
import top.wyhao.security.client.RoleApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户自助操作实现（个人中心 / 注册）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final PasswordEncoder passwordEncoder;
    private final RoleApi roleApi;
    private final MembershipService membershipService;
    private final PasswordApi passwordApi;
    private final FileService fileService;
    private final UserRepository userRepository;
    private final PasswordPolicyConfigApi passwordPolicyConfigApi;
    private final UserDomainService userDomainService;
    private final PasswordPolicyGateway passwordPolicyGateway;
    private final UserLifecycleRules lifecycleRules;

    private static final String[] AVATAR_SUPPORT_SUFFIX = {"jpg", "jpeg", "png", "gif"};
    private static final long AVATAR_MAX_SIZE = 1024 * 1024 * 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateAvatar(MultipartFile avatarFile, Long userId) {
        checkAvatar(avatarFile);

        User user = lifecycleRules.requireUser(userId);
        Long oldAvatarFileId = user.getAvatar();

        File uploaded = fileService.upload(avatarFile, userId);
        userRepository.updateAvatar(userId, uploaded.getId());

        if (oldAvatarFileId != null) {
            try {
                fileService.delete(oldAvatarFileId, userId);
            } catch (Exception e) {
                log.warn("删除旧头像文件失败: fileId={}", oldAvatarFileId, e);
            }
        }

        return uploaded.getId();
    }

    private void checkAvatar(MultipartFile avatarFile) {
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        if (!CharSequenceUtil.equalsAnyIgnoreCase(avatarImageType, AVATAR_SUPPORT_SUFFIX)) {
            throw UserException.avatarFormatNotSupported(String.join(",", AVATAR_SUPPORT_SUFFIX));
        }

        long avatarSize = avatarFile.getSize();
        if (avatarSize > AVATAR_MAX_SIZE) {
            throw UserException.avatarSizeExceeded(AVATAR_MAX_SIZE / 1024 / 1024);
        }
    }

    @Override
    @CacheUpdate(key = "#userId", value = "#req.displayName", name = CacheConstants.USER_KEY_PREFIX)
    public void updateBasicInfo(UserBasicInfoUpdateReq req, Long userId) {
        lifecycleRules.requireUser(userId);
        userRepository.updateBasicInfo(userId, req.getDisplayName(), req.getGender());
    }

    @Override
    public void updatePhone(String newPhone, String oldPassword, Long userId) {
        passwordApi.assertMatches(userId, oldPassword);
        User user = lifecycleRules.requireUser(userId);
        lifecycleRules.assertPhoneChanged(user.getPhone(), newPhone);
        userDomainService.assertPhoneAvailable(newPhone, userId);
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPhone(newPhone);
        userRepository.updatePartial(updateUser);
    }

    @Override
    public void updateEmail(String newEmail, String oldPassword, Long userId) {
        User user = lifecycleRules.requireUser(userId);
        passwordApi.assertMatches(userId, oldPassword);
        lifecycleRules.assertEmailChanged(user.getEmail(), newEmail);
        userDomainService.assertEmailAvailable(newEmail, userId);

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setEmail(newEmail);
        userRepository.updatePartial(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User registerLocal(String username, String rawPassword, Long departmentId, List<Long> roleIds) {
        userDomainService.assertUsernameAvailable(username);
        return persistLocalAccount(username, null, rawPassword, departmentId, roleIds, passwordPolicyGateway.current());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User registerWithEmail(String email, String rawPassword, Long departmentId, List<Long> roleIds) {
        String normalized = email == null ? null : email.trim();
        if (CharSequenceUtil.isBlank(normalized) || normalized.length() > 64 || normalized.indexOf('@') < 1) {
            throw UserException.of("EMAIL_INVALID", "邮箱格式不正确");
        }
        userDomainService.assertEmailAvailable(normalized, null);
        PasswordPolicy policy = loginCompatiblePolicy(passwordPolicyGateway.current());
        EffectivePasswordRules rules = EffectivePasswordRules.from(passwordPolicyConfigApi.get());
        if (!rules.matches(rawPassword)) {
            throw UserException.passwordFormatInvalid(rules.minLength(), rules.maxLength());
        }
        String username = RegisterUsernameAllocator.secure().allocate(
                userRepository::existsUsername,
                rawPassword,
                !policy.allowContainUsername());
        try {
            return persistLocalAccount(username, normalized, rawPassword, departmentId, roleIds, policy);
        } catch (DataIntegrityViolationException ex) {
            // PostgreSQL 在唯一约束失败后会中止当前事务，这里不能再插入。
            // 只把冲突翻译成业务错误，交给外层回滚。
            if (UniqueConflict.targetsEmail(ex)) {
                throw UserException.emailExists();
            }
            if (UniqueConflict.targetsUsername(ex)) {
                throw UserException.of("REGISTER_USERNAME_UNAVAILABLE", "注册暂时无法完成，请稍后重试");
            }
            throw ex;
        }
    }

    /**
     * 注册写入使用的策略窗口与登录格式门对齐。不改网关读出的全局策略。
     */
    private static PasswordPolicy loginCompatiblePolicy(PasswordPolicy raw) {
        int min = Math.min(
                Math.max(raw.minLength(), EffectivePasswordRules.LOGIN_MIN_LENGTH),
                EffectivePasswordRules.LOGIN_MAX_LENGTH);
        return new PasswordPolicy(
                min,
                EffectivePasswordRules.LOGIN_MAX_LENGTH,
                raw.requireSymbols(),
                raw.allowContainUsername(),
                raw.historyRepetitionTimes(),
                raw.expireDays());
    }

    private User persistLocalAccount(String username, String email, String rawPassword, Long departmentId, List<Long> roleIds, PasswordPolicy policy) {
        userDomainService.assertCompliant(
                rawPassword,
                policy,
                username,
                PasswordHistory.empty());
        if (CollUtil.isEmpty(roleIds)) {
            throw UserException.of("REGISTER_ROLE_REQUIRED", "未配置注册默认角色");
        }
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setGender(GenderEnum.UNKNOWN.getValue());
        user.setDepartmentId(departmentId);
        user.setStatus(StatusEnum.ENABLE.getValue());
        user.setIsBuiltin(false);
        user.setPwdUpdateTime(LocalDateTime.now());
        PasswordPolicyConfig passwordPolicy = passwordPolicyConfigApi.get();
        int expireDays = passwordPolicy != null && passwordPolicy.getPasswordExpireDays() != null
                ? passwordPolicy.getPasswordExpireDays()
                : 90;
        user.setPwdExpireDate(LocalDate.now().plusDays(expireDays));
        userRepository.insert(user);
        membershipService.replacePrimaryDepartment(user.getId(), departmentId);
        roleApi.assignRolesToUser(roleIds, user.getId());
        return user;
    }

    @Override
    public User createVerifiedEmailAccount(String username, String email, String passwordHash, Long departmentId, Long roleId) {
        if (passwordHash == null || !passwordHash.startsWith("{")) {
            throw UserException.of("REGISTER_PASSWORD_INVALID", "注册暂时无法完成，请稍后重试");
        }
        if (userRepository.existsEmail(email, null)) {
            throw UserException.emailExists();
        }
        if (roleId == null) {
            throw UserException.of("REGISTER_ROLE_REQUIRED", "未配置注册默认角色");
        }
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(username);
        user.setEmail(email);
        user.setPassword(passwordHash);
        user.setGender(GenderEnum.UNKNOWN.getValue());
        user.setDepartmentId(departmentId);
        user.setStatus(StatusEnum.ENABLE.getValue());
        user.setIsBuiltin(false);
        user.setPwdUpdateTime(LocalDateTime.now());
        PasswordPolicyConfig passwordPolicy = passwordPolicyConfigApi.get();
        int expireDays = passwordPolicy != null && passwordPolicy.getPasswordExpireDays() != null
                ? passwordPolicy.getPasswordExpireDays()
                : 90;
        user.setPwdExpireDate(LocalDate.now().plusDays(expireDays));
        userRepository.insert(user);
        membershipService.replacePrimaryDepartment(user.getId(), departmentId);
        roleApi.assignRolesToUser(List.of(roleId), user.getId());
        return user;
    }
}
