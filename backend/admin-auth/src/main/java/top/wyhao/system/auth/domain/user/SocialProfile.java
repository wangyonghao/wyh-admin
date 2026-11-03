package top.wyhao.system.auth.domain.user;

/**
 * 第三方平台回传的标准化身份（JustAuth 等适配后进入 domain）。
 */
public record SocialProfile(
        String source,
        String openId,
        String username,
        String nickname,
        String genderCode,
        String rawJson
) {}
