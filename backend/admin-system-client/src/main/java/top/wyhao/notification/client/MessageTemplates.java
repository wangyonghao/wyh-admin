package top.wyhao.notification.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 站内信模板（跨模块共享文案与跳转路径）。
 */
@Getter
@RequiredArgsConstructor
public enum MessageTemplates {

    SOCIAL_REGISTER("欢迎注册 %s", "尊敬的 %s，欢迎注册使用，请及时配置您的密码。", "/user/profile"),

    NOTICE_PUBLISH("您有一条新的公告", "公告《%s》已发布，请及时查看。", "/user/notice?id=%s"),

    PASSWORD_EXPIRED("密码已过期", "您的登录密码已过期，为保障账号安全，请尽快前往个人中心修改密码。", "/user/profile"),

    PASSWORD_EXPIRING("密码即将过期", "您的登录密码将在 %s 天后过期，请尽快前往个人中心修改密码。", "/user/profile");

    private final String title;
    private final String content;
    private final String path;
}
