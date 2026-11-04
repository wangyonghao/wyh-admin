package top.wyhao.system.admin.infrastructure.gateway;

import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.AuthRequestFactory;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.SocialOAuthGateway;
import top.wyhao.system.admin.domain.model.SocialIdentity;

@Component
public class SocialOAuthGatewayImpl implements SocialOAuthGateway {

    private final AuthRequestFactory authRequestFactory;

    public SocialOAuthGatewayImpl(AuthRequestFactory authRequestFactory) {
        this.authRequestFactory = authRequestFactory;
    }

    @Override
    public SocialIdentity authenticate(String source, String code, String state) {
        AuthRequest authRequest;
        try {
            authRequest = authRequestFactory.get(source);
        } catch (Exception e) {
            throw UserException.of("PLATFORM_NOT_SUPPORT", "暂不支持 [" + source + "] 平台账号绑定");
        }
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        AuthResponse<AuthUser> response = authRequest.login(callback);
        if (!response.ok()) {
            throw UserException.of("SOCIAL_AUTH_FAILED", "三方账号授权失败：" + response.getMsg());
        }
        AuthUser authUser = response.getData();
        return new SocialIdentity(
                authUser.getSource(),
                authUser.getUuid(),
                authUser.getUsername(),
                JSONUtil.toJsonStr(authUser));
    }
}
