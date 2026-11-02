package top.wyhao.system.admin.domain.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.PasswordPolicyGateway;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.CreateUserCommand;
import top.wyhao.system.admin.domain.model.PasswordHistory;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户领域服务：创建不变量、唯一性、密码合规与领域计算（不含持久化）。
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordPolicyGateway passwordPolicyGateway;

    /**
     * 校验 + 取策略 + 密码加密 + 构建可插入的 {@link User}（不写库）。
     *
     * <p>{@code policy.expireDays() <= 0} 表示不设过期。
     */
    public User build(CreateUserCommand cmd) {
        PasswordPolicy policy = passwordPolicyGateway.current();
        assertCompliant(cmd.rawPassword(), policy, cmd.username(), PasswordHistory.empty());
        assertUsernameAvailable(cmd.username());
        assertEmailAvailable(cmd.email(), null);
        assertPhoneAvailable(cmd.phone(), null);

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(cmd.username());
        user.setDisplayName(cmd.displayName());
        user.setPassword(passwordEncoder.encode(cmd.rawPassword()));
        user.setEmail(cmd.email());
        user.setPhone(cmd.phone());
        user.setGender(cmd.gender());
        user.setDepartmentId(cmd.departmentId());
        user.setStatus(cmd.status());
        user.setDescription(cmd.description());
        user.setAvatar(cmd.avatar());
        user.setPwdUpdateTime(now);
        user.setPwdExpireDate(resolveExpireDate(policy, LocalDate.now()));
        return user;
    }

    public void assertUsernameAvailable(String username) {
        if (userRepository.existsUsername(username)) {
            throw UserException.usernameExists();
        }
    }

    public void assertEmailAvailable(String email, Long excludeUserId) {
        if (userRepository.existsEmail(email, excludeUserId)) {
            throw UserException.emailExists();
        }
    }

    public void assertPhoneAvailable(String phone, Long excludeUserId) {
        if (userRepository.existsPhone(phone, excludeUserId)) {
            throw UserException.phoneExists();
        }
    }

    /**
     * 校验明文密码是否满足策略；不通过则抛 {@link UserException}。
     */
    public void assertCompliant(String rawPassword, PasswordPolicy policy, String username, PasswordHistory history) {
        assertMinLengthAndFormat(rawPassword, policy);
        assertSymbolsIfRequired(rawPassword, policy);
        assertNotContainUsername(rawPassword, policy, username);
        assertNotInHistory(rawPassword, policy, history);
    }

    private void assertMinLengthAndFormat(String rawPassword, PasswordPolicy policy) {
        int minLength = policy.minLength();
        int maxLength = policy.maxLength();
        if (StrUtil.length(rawPassword) < minLength) {
            throw UserException.passwordPolicyViolated("密码最小长度为 %d 个字符".formatted(minLength));
        }
        if (!ReUtil.isMatch(RegexConstants.PASSWORD_TEMPLATE.formatted(minLength, maxLength), rawPassword)) {
            throw UserException.passwordFormatInvalid(minLength, maxLength);
        }
    }

    private void assertSymbolsIfRequired(String rawPassword, PasswordPolicy policy) {
        if (policy.requireSymbols() && !ReUtil.contains(RegexConstants.SPECIAL_CHARACTER, rawPassword)) {
            throw UserException.passwordPolicyViolated("密码必须包含特殊字符");
        }
    }

    private void assertNotContainUsername(String rawPassword, PasswordPolicy policy, String username) {
        if (policy.allowContainUsername() || CharSequenceUtil.isBlank(username)) {
            return;
        }
        if (CharSequenceUtil.containsAnyIgnoreCase(rawPassword, username, StrUtil.reverse(username))) {
            throw UserException.passwordPolicyViolated("密码不允许包含正反序用户名");
        }
    }

    private void assertNotInHistory(String rawPassword, PasswordPolicy policy, PasswordHistory history) {
        int times = policy.historyRepetitionTimes();
        PasswordHistory effective = history == null ? PasswordHistory.empty() : history;
        if (effective.isReused(rawPassword, passwordEncoder, times)) {
            throw UserException.passwordPolicyViolated("新密码不得与历史前 %d 次密码重复".formatted(times));
        }
    }

    private static LocalDate resolveExpireDate(PasswordPolicy policy, LocalDate today) {
        int days = policy.expireDays();
        if (days <= 0) {
            return null;
        }
        return today.plusDays(days);
    }
}
