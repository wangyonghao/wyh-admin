
package top.wyhao.cmn.db.autoconfigure;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.type.OffsetDateTimeTypeHandler;
import org.apache.ibatis.type.OffsetTimeTypeHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.wyhao.starter.core.constant.PropertiesConstants;
import top.wyhao.starter.core.util.GeneralPropertySourceFactory;
import top.wyhao.cmn.db.autoconfigure.idgenerator.CosIdGenerator;
import top.wyhao.cmn.db.datapermission.handler.DefaultDataPermissionHandler;
import top.wyhao.cmn.db.encrypt.EncryptTypeHandler;
import top.wyhao.cmn.db.handler.CompositeBaseEnumTypeHandler;
import top.wyhao.cmn.db.handler.MyBatisPlusMetaObjectHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MyBatis Plus 自动配置
 *
 * @author Charles7c
 * @since 1.0.0
 */
@AutoConfiguration
@MapperScan("${mybatis-plus.extension.mapper-package}")
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties(MyBatisPlusExtensionProperties.class)
@ConditionalOnProperty(prefix = "mybatis-plus.extension", name = PropertiesConstants.ENABLED, havingValue = "true")
@PropertySource(value = "classpath:default-data-mybatis-plus.yml", factory = GeneralPropertySourceFactory.class)
public class MybatisPlusAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MybatisPlusAutoConfiguration.class);

    @Value("${application.crypto.aes-key}")
    private String aesKey;

    /**
     * MyBatis Plus 配置
     *
     * @since 2.4.0
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> properties.getConfiguration()
                .setDefaultEnumTypeHandler(CompositeBaseEnumTypeHandler.class);
    }

    /**
     * MyBatis Plus 插件配置
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MyBatisPlusExtensionProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 其他拦截器
        Map<String, InnerInterceptor> innerInterceptors = SpringUtil.getBeansOfType(InnerInterceptor.class);
        if (!innerInterceptors.isEmpty()) {
            innerInterceptors.values().forEach(interceptor::addInnerInterceptor);
        }
        // 分页插件
        MyBatisPlusExtensionProperties.PaginationProperties paginationProperties = properties.getPagination();
        if (paginationProperties != null && paginationProperties.isEnabled()) {
            interceptor.addInnerInterceptor(this.paginationInnerInterceptor(paginationProperties));
        }
        // 乐观锁插件
        if (properties.isOptimisticLockerEnabled()) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }
        // 防全表更新与删除插件
        if (properties.isBlockAttackPluginEnabled()) {
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        }
        return interceptor;
    }

    /**
     * ID 生成器配置
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new CosIdGenerator();
    }

    /**
     * 分页插件配置（<a href="https://baomidou.com/pages/97710a/#paginationinnerinterceptor">PaginationInnerInterceptor</a>）
     */
    private PaginationInnerInterceptor paginationInnerInterceptor(MyBatisPlusExtensionProperties.PaginationProperties paginationProperties) {
        // 对于单一数据库类型来说，都建议配置该值，避免每次分页都去抓取数据库类型
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(paginationProperties
                .getDbType());
        paginationInnerInterceptor.setOverflow(paginationProperties.isOverflow());
        paginationInnerInterceptor.setMaxLimit(paginationProperties.getMaxLimit());
        return paginationInnerInterceptor;
    }

    /**
     * 数据权限拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionInterceptor dataPermissionInterceptor(DataPermissionHandler dataPermissionHandler) {
        return new DataPermissionInterceptor(dataPermissionHandler);
    }

    /**
     * 数据权限处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionHandler dataPermissionHandler() {
        return new DefaultDataPermissionHandler();
    }

    // SQL 解析本地缓存
    static {
        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(cache -> cache.maximumSize(1024)
                .expireAfterWrite(5, TimeUnit.SECONDS)));
    }

    /**
     * 元对象处理器配置（插入或修改时自动填充）
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyBatisPlusMetaObjectHandler();
    }

    /**
     * 注册到 MyBatis 类型处理器注册中心
     */
    @Bean
    public ConfigurationCustomizer typeHandlerRegistryCustomizer() {
        return configuration -> {
            if(aesKey != null){
                EncryptTypeHandler.init(aesKey);  // 为 EncryptTypeHandler 初始化 AES 密钥
                log.info("[cmn-db] EncryptTypeHandler - AES密钥初始化完成，密钥长度：{}", aesKey.length());
                configuration.getTypeHandlerRegistry().register(EncryptTypeHandler.class);
            }
        };
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("[cmn-db] - 'MyBatis Plus' configured.");
    }

}
