package top.wyhao.system.auth.infrastructure.gateway;

import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.domain.gateway.SocialAuthGateway;
import top.wyhao.system.auth.domain.user.SocialProfile;

@Component
public class SocialAuthGatewayImpl implements SocialAuthGateway {

    private final JustAuthProperties authProperties;

    public SocialAuthGatewayImpl(JustAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public SocialProfile authenticate(String source, String code, String state) {
        AuthRequest authRequest = buildAuthRequest(source);
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        AuthResponse<AuthUser> response = authRequest.login(callback);
        if (!response.ok()) {
            throw AuthenticationException.socialAuthFailed(response.getMsg());
        }
        AuthUser authUser = response.getData();
        String genderCode = null;
        if (authUser.getGender() != null) {
            genderCode = authUser.getGender().getCode();
        }
        return new SocialProfile(
                authUser.getSource(),
                authUser.getUuid(),
                authUser.getUsername(),
                authUser.getNickname(),
                genderCode,
                JSONUtil.toJsonStr(authUser));
    }

    private AuthRequest buildAuthRequest(String source) {
        try {
            AuthConfig authConfig = authProperties.getType().get(source.toUpperCase());
            return AuthRequestBuilder.builder().source(source).authConfig(authConfig).build();
        } catch (Exception e) {
            throw AuthenticationException.platformNotSupport(source);
        }
    }
}
