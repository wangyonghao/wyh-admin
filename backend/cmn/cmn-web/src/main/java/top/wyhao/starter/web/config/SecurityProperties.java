
package top.wyhao.starter.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SaToken 安全配置属性
 *

 * @since 1.0.0
 */
@ConfigurationProperties("web.security")
public class SecurityProperties {

    /**
     * 排除（放行）路径配置
     */
    private String[] excludes;

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}