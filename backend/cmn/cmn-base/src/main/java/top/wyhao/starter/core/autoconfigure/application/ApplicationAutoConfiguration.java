
package top.wyhao.starter.core.autoconfigure.application;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 应用自动配置
 *

 * @since 1.0.0
 */
@AutoConfiguration
@ComponentScan("cn.hutool.extra.spring")
@Import(cn.hutool.extra.spring.SpringUtil.class)
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationAutoConfiguration {}
