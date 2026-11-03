package top.wyhao.system.admin.app.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.identity.client.PasswordApi;
import top.wyhao.identity.client.PasswordPolicyConfig;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.PasswordHistory;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.domain.service.UserDomainService;
import top.wyhao.redisson.util.RedisUtils;

import java.time.LocalDateTime;

/**
 * 密码重置、修改与策略校验。
 */
@Service
@RequiredArgsConstructor
public class PasswordService implements PasswordApi {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordPolicyConfigApi passwordPolicyConfigApi;
    private final UserDomainService userDomainService;

    @Override
    public void resetPassword(Long userId, String rawPassword) {
        PasswordPolicy policy = toPolicy(passwordPolicyConfigApi.get());
        userRepository.updatePasswordAndExpire(userId, passwordEncoder.encode(rawPassword), LocalDateTime.now()
                .plusDays(policy.expireDays()));
    }

    @Override
    public String resetToRandomPassword(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw UserException.notFound();
        }
        String newPassword = generateSecurePassword();
        resetPassword(userId, newPassword);
        RedisUtils.deleteByPattern("login:retry:" + user.getUsername() + ":*");
        return newPassword;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (ObjectUtil.equal(newPassword, oldPassword)) {
            throw UserException.passwordSameAsOld();
        }
        User oldUser = userRepository.findById(userId);
        if (oldUser == null) {
            throw UserException.passwordIncorrect();
        }
        if (CharSequenceUtil.isNotBlank(oldUser.getPassword())
                && !passwordEncoder.matches(oldPassword, oldUser.getPassword())) {
            throw UserException.passwordIncorrect();
        }
        PasswordPolicy policy = toPolicy(passwordPolicyConfigApi.get());
        PasswordHistory history = PasswordHistory.parse(oldUser.getPasswordHistory());
        userDomainService.assertCompliant(newPassword, policy, oldUser.getUsername(), history);
        userRepository.updatePassword(
                userId,
                passwordEncoder.encode(newPassword),
                LocalDateTime.now(),
                history.push(oldUser.getPassword()).serialize()
        );
        LoginUtil.logout();
    }

    @Override
    public void assertMatches(Long userId, String rawPassword) {
        User user = userRepository.findById(userId);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw UserException.passwordIncorrect();
        }
    }

    private PasswordPolicy toPolicy(PasswordPolicyConfig config) {
        if (config == null) {
            return new PasswordPolicy(
                    PasswordPolicy.MIN_LENGTH_LOWER,
                    PasswordPolicy.DEFAULT_MAX_LENGTH,
                    false,
                    false,
                    PasswordPolicy.REPETITION_TIMES_LOWER,
                    90
            );
        }
        return new PasswordPolicy(
                ObjectUtil.defaultIfNull(config.getPasswordMinLength(), PasswordPolicy.MIN_LENGTH_LOWER),
                PasswordPolicy.DEFAULT_MAX_LENGTH,
                Boolean.TRUE.equals(config.getPasswordRequireSpecial()),
                Boolean.TRUE.equals(config.getPasswordAllowContainUsername()),
                ObjectUtil.defaultIfNull(config.getPasswordRepetitionTimes(), PasswordPolicy.REPETITION_TIMES_LOWER),
                ObjectUtil.defaultIfNull(config.getPasswordExpireDays(), 90)
        );
    }

    private String generateSecurePassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = upperCase + lowerCase + digits + specialChars;

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder(12);
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        return new String(passwordArray);
    }
}
