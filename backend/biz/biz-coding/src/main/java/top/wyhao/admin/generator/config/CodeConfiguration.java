
package top.wyhao.admin.generator.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成配置

 * @since 2025/6/14 21:22
 */
@Configuration
public class CodeConfiguration {

    /**
     * API 文档分组配置
     */
    @Bean
    public GroupedOpenApi codeApi() {
        return GroupedOpenApi.builder().group("code").displayName("代码生成").pathsToMatch("/code/**").build();
    }
}
