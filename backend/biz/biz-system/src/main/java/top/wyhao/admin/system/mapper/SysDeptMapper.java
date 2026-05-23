
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.cmn.db.dialect.DatabaseType;
import top.wyhao.cmn.db.model.BaseMapper;

import java.util.List;

/**
 * 部门 Mapper
 *

 * @since 2023/1/22 17:56
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    default List<SysDept> listChildren(Long id) {
        return this.lambdaQuery().apply(DatabaseType.POSTGRE_SQL.findInSet(id, "ancestors")).list();
    }

}
