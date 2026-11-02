package top.wyhao.system.auth.app.dto;

import cn.hutool.core.util.StrUtil;
import jakarta.validation.constraints.NotNull;
import top.wyhao.system.auth.domain.model.ClientContext;
import top.wyhao.system.auth.domain.user.PasswordPolicy;

/**
 * 账号密码登录命令。
 */
public record PasswordLoginCommand(
        String username,
        String email,
        String phone,
        String password,
        String captchaUuid,
        String captcha,
        ClientContext client,
        PasswordPolicy policy
) {

    @NotNull(message = "请提供username、email或phone中的任意一项")
    public String identifier(){
        if(StrUtil.isNotBlank(username)) return username;
        if(StrUtil.isNotBlank(email)) return email;
        if(StrUtil.isNotBlank(phone)) return phone;
        return null;
    }
}
