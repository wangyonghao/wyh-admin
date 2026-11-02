package top.wyhao.system.auth.domain.user;

import cn.hutool.core.util.ObjectUtil;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.system.auth.domain.exception.AuthenticationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 登录与密码校验需要的用户凭证，不包含档案管理字段。
 */
public record CredentialUser(
        Long id,
        String username,
        String displayName,
        String password,
        Integer status,
        Integer gender,
        Long departmentId,
        LocalDateTime pwdUpdateTime,
        LocalDate pwdExpireDate
) {
    public boolean canLogin(){
        if (ObjectUtil.equal(StatusEnum.DISABLE.getValue(), status)) {
            throw AuthenticationException.accountDisabled();
        }
        return true;
    }
    public boolean isPasswordExpired(LocalDate today) {
        return pwdExpireDate != null && pwdExpireDate.isBefore(today);
    }

    /**
     * 密码尚未过期，但将在 warnDays 天内过期（含当天到期）。
     */
    public boolean isPasswordExpiringSoon(LocalDate today, int warnDays) {
        if (pwdExpireDate == null || warnDays < 1 || isPasswordExpired(today)) {
            return false;
        }
        return !pwdExpireDate.isAfter(today.plusDays(warnDays));
    }
}
