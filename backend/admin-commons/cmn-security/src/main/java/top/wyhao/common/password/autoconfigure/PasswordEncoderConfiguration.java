package top.wyhao.common.password.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码哈希算法（基础设施）。业务域只注入 {@link PasswordEncoder}，不在 biz 模块声明 Bean。
 */
@AutoConfiguration
public class PasswordEncoderConfiguration {

    /**
     * 哈希前面带算法前缀，便于以后平滑升级算法
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Spring Security 中推荐使用 DelegatingPasswordEncoder，它在哈希前面带算法前缀，便于以后平滑升级算法
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
