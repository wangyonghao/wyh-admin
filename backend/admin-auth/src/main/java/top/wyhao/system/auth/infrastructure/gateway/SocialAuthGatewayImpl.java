package top.wyhao.system.auth.infrastructure.gateway;

import cn.hutool.json.JSONUtil;
import com.xkcoding.justauth.AuthRequestFactory;
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

    private final AuthRequestFactory authRequestFactory;

    public SocialAuthGatewayImpl(AuthRequestFactory authRequestFactory) {
        this.authRequestFactory = authRequestFactory;
    }

    @Override
    public SocialProfile authenticate(String source, String code, String state) {
        AuthRequest authRequest;
        try {
            authRequest = authRequestFactory.get(source);
        } catch (Exception e) {
            throw AuthenticationException.platformNotSupport(source);
        }
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
}
