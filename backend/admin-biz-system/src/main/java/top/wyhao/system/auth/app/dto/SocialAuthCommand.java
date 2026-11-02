package top.wyhao.system.auth.app.dto;

import top.wyhao.system.auth.domain.model.ClientContext;

/**
 * 第三方登录命令。
 */
public record SocialAuthCommand(
        String provider,
        String code,
        String state,
        ClientContext client
) {}
