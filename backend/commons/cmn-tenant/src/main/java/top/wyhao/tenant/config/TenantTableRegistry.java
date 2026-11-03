package top.wyhao.tenant.config;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.springframework.beans.factory.InitializingBean;
import top.wyhao.tenant.core.TenantedEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TenantTableRegistry implements InitializingBean {
    private final Set<String> tenantedTables = new HashSet<>();
    private int knownTableCount = -1;

    @Override
    public void afterPropertiesSet() {
        refreshIfNeeded();
    }

    /**
     * 显式登记租户表。单测和启动扫描都会走这里。
     */
    public void register(String tableName) {
        tenantedTables.add(normalize(tableName));
    }

    public boolean isTenantedTable(String tableName) {
        refreshIfNeeded();
        return tenantedTables.contains(normalize(tableName));
    }

    /**
     * Mapper 解析可能晚于本 bean 的初始化，表信息变多时再扫一遍。
     */
    private void refreshIfNeeded() {
        List<TableInfo> infos;
        try {
            infos = TableInfoHelper.getTableInfos();
        } catch (RuntimeException ex) {
            return;
        }
        if (infos == null || infos.size() == knownTableCount) {
            return;
        }
        for (TableInfo tableInfo : infos) {
            Class<?> entityType = tableInfo.getEntityType();
            if (entityType != null && TenantedEntity.class.isAssignableFrom(entityType)) {
                tenantedTables.add(tableInfo.getTableName());
            }
        }
        knownTableCount = infos.size();
    }

    private static String normalize(String tableName) {
        if (tableName == null) {
            return "";
        }
        String name = tableName.trim();
        if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }
}
