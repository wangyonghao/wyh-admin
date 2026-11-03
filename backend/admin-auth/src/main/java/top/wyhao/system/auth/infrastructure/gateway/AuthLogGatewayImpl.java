package top.wyhao.system.auth.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import top.wyhao.audit.client.LoginEvent;
import top.wyhao.system.auth.domain.gateway.AuthLogGateway;
import top.wyhao.tenant.context.TenantContextHolder;

/**
 * 登录审计：仅发布 {@link LoginEvent}，落库与展示归 System。
 */
@Component
@RequiredArgsConstructor
public class AuthLogGatewayImpl implements AuthLogGateway {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void recordSuccess(String username, String ip, String userAgent) {
        eventPublisher.publishEvent(
                LoginEvent.success(username, ip, userAgent, TenantContextHolder.getTenantId()));
    }

    @Override
    public void recordFailure(String username, String ip, String userAgent, String reason) {
        eventPublisher.publishEvent(
                LoginEvent.failure(username, ip, userAgent, reason, TenantContextHolder.getTenantId()));
    }
}
