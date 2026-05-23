
package top.wyhao.starter.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

import io.undertow.Undertow;
import io.undertow.server.handlers.DisallowedMethodsHandler;
import io.undertow.util.HttpString;
import org.springframework.context.annotation.PropertySource;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.GeneralPropertySourceFactory;

import java.util.Set;

/**
 * Undertow 自动配置
 *


 * @since 2.11.0
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(Undertow.class)
@PropertySource(value = "classpath:default-server.yml", factory = GeneralPropertySourceFactory.class)
public class UndertowConfig {

    private static final Logger log = LoggerFactory.getLogger(UndertowConfig.class);

    /**
     * 默认禁止三个不安全的 HTTP 方法（如 CONNECT、TRACE、TRACK）
     */
    private static final Set<HttpString> DEFAULT_DISALLOWED_METHODS = Set.of(
            HttpString.tryFromString("CONNECT"),
            HttpString.tryFromString("TRACE"),
            HttpString.tryFromString("TRACK"));

    /**
     * Undertow 自定义配置
     */
    @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> customize() {
        return factory -> {
            factory.addDeploymentInfoCustomizers(deploymentInfo -> deploymentInfo
                    .addInitialHandlerChainWrapper(handler -> new DisallowedMethodsHandler(handler, DEFAULT_DISALLOWED_METHODS)));
            log.debug("[cmn-web] - Disallowed HTTP methods on Server Undertow: {}.", DEFAULT_DISALLOWED_METHODS);
        };
    }
}
