
package top.wyhao.starter.cache.redisson.autoconfigure;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import top.wyhao.starter.core.util.GeneralPropertySourceFactory;

/**
 * JetCache 自动配置
 *

 * @since 2026/5/14
 */
@AutoConfiguration
@Import(com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration.class)
@PropertySource(value = "classpath:default-cache-jetcache.yml", factory = GeneralPropertySourceFactory.class)
public class JetCacheAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JetCacheAutoConfiguration.class);

    @PostConstruct
    public void postConstruct() {
        log.debug("[cmn-cache] - 'JetCache' configured.");
    }

}
