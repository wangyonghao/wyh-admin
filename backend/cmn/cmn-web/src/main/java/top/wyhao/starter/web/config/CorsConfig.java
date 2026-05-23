
package top.wyhao.starter.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import top.wyhao.starter.core.constant.PropertiesConstants;
import top.wyhao.starter.core.constant.StringConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域自动配置
 *

 * @since 1.0.0
 */
@Lazy
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = PropertiesConstants.WEB_CORS, name = PropertiesConstants.ENABLED, havingValue = "true")
@EnableConfigurationProperties(CorsConfig.CorsProperties.class)
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    /**
     * 跨域过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public CorsFilter corsFilter(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        // 设置跨域允许时间(秒）
        config.setMaxAge(1800L);
        // 配置允许跨域的域名
        if (properties.getAllowedOrigins().contains(StringConstants.ASTERISK)) {
            config.addAllowedOriginPattern(StringConstants.ASTERISK);
        } else {
            // 配置为 true 后则必须配置允许跨域的域名，且不允许配置为 *
            config.setAllowCredentials(true);
            properties.getAllowedOrigins().forEach(config::addAllowedOrigin);
        }
        // 配置允许跨域的请求方式
        properties.getAllowedMethods().forEach(config::addAllowedMethod);
        // 配置允许跨域的请求头
        properties.getAllowedHeaders().forEach(config::addAllowedHeader);
        // 配置允许跨域的响应头
        properties.getExposedHeaders().forEach(config::addExposedHeader);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(StringConstants.PATH_PATTERN, config);
        CorsFilter corsFilter = new CorsFilter(source);
        log.debug("[cmn-web] - 'Web-CorsFilter' configured.");
        return corsFilter;
    }


    /**
     * 跨域配置属性
     */
    @ConfigurationProperties(PropertiesConstants.WEB_CORS)
    class CorsProperties {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 允许跨域的域名
         */
        private List<String> allowedOrigins = List.of("*");

        /**
         * 允许跨域的请求方式
         */
        private List<String> allowedMethods = List.of("*");

        /**
         * 允许跨域的请求头
         */
        private List<String> allowedHeaders = List.of("*");

        /**
         * 允许跨域的响应头
         */
        private List<String> exposedHeaders = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }
    }

}
