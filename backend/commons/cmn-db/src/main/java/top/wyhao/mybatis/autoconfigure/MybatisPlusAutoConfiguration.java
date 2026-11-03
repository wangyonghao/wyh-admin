
package top.wyhao.mybatis.autoconfigure;

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
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import top.wyhao.mybatis.autoconfigure.idgenerator.CosIdGenerator;
import top.wyhao.mybatis.datapermission.handler.DefaultDataPermissionHandler;
import top.wyhao.mybatis.encrypt.EncryptTypeHandler;
import top.wyhao.mybatis.handler.CompositeBaseEnumTypeHandler;
import top.wyhao.mybatis.handler.MyBatisPlusMetaObjectHandler;

import java.util.concurrent.TimeUnit;

import static com.baomidou.mybatisplus.annotation.DbType.POSTGRE_SQL;

/**
 * MyBatis-Plus 自动配置
 *
 * @since 1.0.0
 */
@AutoConfiguration
@MapperScan("${application.base-package}.**.mapper")
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisPlusAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MybatisPlusAutoConfiguration.class);

    @Value("${application.crypto.aes-key}")
    private String aesKey;

    /**
     * MyBatis-Plus 配置 (<a href="https://baomidou.com/reference/">使用配置</a>)
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            // 启动时检查 MyBatis XML 文件是否存在
            properties.setCheckConfigLocation(true);
            properties.getConfiguration()
                    // 默认枚举类型处理器（扩展 BaseEnum 支持）
                    .setDefaultEnumTypeHandler(CompositeBaseEnumTypeHandler.class)
                    // 开启驼峰命名映射
                    .setMapUnderscoreToCamelCase(true)
                    // 自动映射未知列处理策略：不做任何处理
                    .setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.NONE)
                    // 日志实现：关闭
                    .setLogImpl(NoLoggingImpl.class);
        };
    }

    /**
     * MyBatis Plus 插件配置
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            DataPermissionInterceptor dataPermissionInterceptor,
            ObjectProvider<TenantLineInnerInterceptor> tenantLineInnerInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 租户行条件要先于分页和数据权限，否则分页 count 与权限 SQL 看不到租户谓词
        tenantLineInnerInterceptor.ifAvailable(interceptor::addInnerInterceptor);
        interceptor.addInnerInterceptor(dataPermissionInterceptor);
        interceptor.addInnerInterceptor(this.paginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
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
    private PaginationInnerInterceptor paginationInnerInterceptor() {
        // 对于单一数据库类型来说，都建议配置该值，避免每次分页都去抓取数据库类型
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(POSTGRE_SQL);
        // 单页上限，防止 pageSize=99999
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 页码超出总页数时返回空而不是回到首页
        paginationInnerInterceptor.setOverflow(false);
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
     * 只初始化 AES 密钥，不要把 EncryptTypeHandler 注册成 String/VARCHAR 的默认处理器。
     * 该类带 @MappedTypes(String.class)，全局 register 后所有 varchar 都会走 AES 解密；
     * 中文明文（菜单名、角色名）会被 Hutool 当成 Base64 解成空串，ASCII 路径/编码则解密失败后回退原文。
     * 需要加解密的字段用 @TableField(typeHandler = EncryptTypeHandler.class)。
     */
    @Bean
    public ConfigurationCustomizer typeHandlerRegistryCustomizer() {
        return configuration -> {
            if (aesKey != null) {
                EncryptTypeHandler.init(aesKey);
                log.info("[cmn-mybatis] EncryptTypeHandler - AES密钥初始化完成，密钥长度：{}", aesKey.length());
            }
        };
    }
}
