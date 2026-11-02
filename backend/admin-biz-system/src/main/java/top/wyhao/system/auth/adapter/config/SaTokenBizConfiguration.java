package top.wyhao.system.auth.adapter.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wyhao.security.client.PermissionApi;

/**
 * 业务侧 Sa-Token 权限桥接（依赖 PermissionApi，不放进 commons）。
 */
@Configuration
public class SaTokenBizConfiguration {

    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface(PermissionApi permissionProvider) {
        return new StpInterfaceImpl(permissionProvider);
    }
}
