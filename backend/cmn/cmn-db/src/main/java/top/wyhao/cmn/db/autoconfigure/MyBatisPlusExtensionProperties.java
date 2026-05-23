
package top.wyhao.cmn.db.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * MyBatis Plus 扩展配置属性
 *

 * @since 1.0.0
 */
@ConfigurationProperties("mybatis-plus.extension")
public class MyBatisPlusExtensionProperties {

    /**
     * 是否启用
     */
    private boolean enabled = false;

    /**
     * Mapper 接口扫描包（配置时必须使用：mapper-package 键名）
     * <p>
     * e.g. com.example.**.mapper
     * </p>
     */
    private String mapperPackage;

    /**
     * 分页插件配置
     */
    private PaginationProperties pagination;

    /**
     * 启用乐观锁插件
     */
    private boolean optimisticLockerEnabled = false;

    /**
     * 启用防全表更新与删除插件
     */
    private boolean blockAttackPluginEnabled = true;

    /**
     * 分页插件配置属性
     */
    public static class PaginationProperties {

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 数据库类型
         */
        private DbType dbType;

        /**
         * 是否溢出处理
         */
        private boolean overflow = false;

        /**
         * 单页分页条数限制（默认：-1 表示无限制）
         */
        private Long maxLimit = -1L;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public DbType getDbType() {
            return dbType;
        }

        public void setDbType(DbType dbType) {
            this.dbType = dbType;
        }

        public boolean isOverflow() {
            return overflow;
        }

        public void setOverflow(boolean overflow) {
            this.overflow = overflow;
        }

        public Long getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(Long maxLimit) {
            this.maxLimit = maxLimit;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMapperPackage() {
        return mapperPackage;
    }

    public void setMapperPackage(String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }

    public PaginationProperties getPagination() {
        return pagination;
    }

    public void setPagination(PaginationProperties pagination) {
        this.pagination = pagination;
    }

    public boolean isOptimisticLockerEnabled() {
        return optimisticLockerEnabled;
    }

    public void setOptimisticLockerEnabled(boolean optimisticLockerEnabled) {
        this.optimisticLockerEnabled = optimisticLockerEnabled;
    }

    public boolean isBlockAttackPluginEnabled() {
        return blockAttackPluginEnabled;
    }

    public void setBlockAttackPluginEnabled(boolean blockAttackPluginEnabled) {
        this.blockAttackPluginEnabled = blockAttackPluginEnabled;
    }
}
