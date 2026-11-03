package top.wyhao.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisson;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import top.wyhao.satoken.handler.SaTokenExceptionHandler;

/**
 * Sa-Token 自动配置。
 * <p>权限接口 {@code StpInterface} 由业务模块（admin-biz-system）基于 PermissionApi 提供。
 */
@AutoConfiguration
public class SaTokenConfig {

    /**
     * 整合 JWT（简单模式）
     */
    @Bean
    @ConditionalOnMissingBean
    public StpLogic stpLogic() {
        return new StpLogicJwtForSimple();
    }

    /**
     * Token 持久层配置
     */
    @Bean
    @ConditionalOnMissingBean
    public SaTokenDao saTokenDao(RedissonClient redissonClient) {
        return new SaTokenDaoForRedisson(redissonClient);
    }

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }
}
