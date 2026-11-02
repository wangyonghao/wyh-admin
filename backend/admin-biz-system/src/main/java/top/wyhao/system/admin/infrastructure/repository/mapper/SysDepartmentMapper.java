package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysDepartmentDO;
import top.wyhao.mybatis.model.BaseMapper;

import java.util.List;

/**
 * 部门 Mapper
 *
 * @since 2023/1/22 17:56
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartmentDO> {

    /**
     * 查询指定部门的所有后代（ancestors 逗号分隔路径，PostgreSQL）。
     */
    default List<SysDepartmentDO> listChildren(Long id) {
        return this.lambdaQuery()
                .apply("(select position(',{0},' in ','||ancestors||',')) <> 0", id)
                .list();
    }

}
