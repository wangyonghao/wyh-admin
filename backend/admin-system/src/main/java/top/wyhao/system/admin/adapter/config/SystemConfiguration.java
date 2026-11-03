package top.wyhao.system.admin.adapter.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统壳配置（OpenAPI 分组等）。密码哈希 Bean 在 cmn-security。
 */
@Configuration
public class SystemConfiguration {

    /**
     * API 文档分组配置
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("system").displayName("系统管理").pathsToMatch("/system/**").build();
    }
}
