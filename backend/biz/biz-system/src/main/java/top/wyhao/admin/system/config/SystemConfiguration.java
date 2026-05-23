
package top.wyhao.admin.system.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 系统管理配置
 *

 * @since 2025/6/14 21:22
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

    /**
     * 密码加密存储算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        /*
         *  可用算法：
         *   BCRYPT - new BCryptPasswordEncoder()
         *   SCRYPT - SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8()
         *   PBKDF2 - Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()
         *   ARGON2 - Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
         */
        return new BCryptPasswordEncoder();
    }
}
