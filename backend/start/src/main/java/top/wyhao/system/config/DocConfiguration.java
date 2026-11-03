
package top.wyhao.system.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 文档
 */
@Configuration
public class DocConfiguration {

    /**
     * 认证 API 文档分组配置
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("auth")
            .displayName("系统认证")
            .pathsToMatch("/auth/**", "/monitor/online/**")
            .build();
    }

    /**
     * 开放 API 文档分组配置
     */
    @Bean
    public GroupedOpenApi openApi() {
        return GroupedOpenApi.builder().group("open").displayName("能力开放").pathsToMatch("/open/**").build();
    }
}
