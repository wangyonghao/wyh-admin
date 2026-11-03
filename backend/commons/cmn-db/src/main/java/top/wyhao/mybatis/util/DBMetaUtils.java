
package top.wyhao.mybatis.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.db.DbRuntimeException;
import cn.hutool.db.DbUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import cn.hutool.db.meta.TableType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 数据库元数据信息工具类
 */
public class DBMetaUtils {

    private DBMetaUtils() {
    }

    /**
     * 获取所有表信息
     *
     * @param dataSource 数据源
     * @return 表信息列表
     */
    public static List<Table> getTables(DataSource dataSource) {
        return getTables(dataSource, null);
    }

    /**
     * 获取所有表信息
     *
     * @param dataSource 数据源
     * @param tableName  表名称
     * @return 表信息列表
    
     * @since 2.7.2
     */
    public static List<Table> getTables(DataSource dataSource, String tableName) {
        List<Table> tables = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String catalog = MetaUtil.getCatalog(conn);
            String schema = MetaUtil.getSchema(conn);
            final DatabaseMetaData metaData = conn.getMetaData();
            try (final ResultSet rs = metaData.getTables(catalog, schema, tableName, Convert
                .toStrArray(TableType.TABLE))) {
                if (rs != null) {
                    String name;
                    while (rs.next()) {
                        name = rs.getString("TABLE_NAME");
                        if (CharSequenceUtil.isNotBlank(name)) {
                            final Table table = Table.create(name);
                            table.setCatalog(catalog);
                            table.setSchema(schema);
                            table.setComment(MetaUtil.getRemarks(metaData, catalog, schema, name));
                            tables.add(table);
                        }
                    }
                }
            }
            return tables;
        } catch (Exception e) {
            throw new DbRuntimeException("Get tables error!", e);
        } finally {
            DbUtil.close(conn);
        }
    }

    /**
     * 获取所有列信息
     *
     * @param dataSource 数据源
     * @param tableName  表名称
     * @return 列信息列表
     */
    public static Collection<Column> getColumns(DataSource dataSource, String tableName) {
        Table table = MetaUtil.getTableMeta(dataSource, tableName);
        return table.getColumns();
    }
}
