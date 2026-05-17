
package top.wyhao.admin.generator.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.admin.generator.model.entity.GenFieldConfig;
import top.wyhao.cmn.db.model.BaseMapper;

import java.util.List;

/**
 * 字段配置 Mapper
 *
 * @author Charles7c
 * @since 2023/4/12 23:56
 */
@Mapper
public interface FieldConfigMapper extends BaseMapper<GenFieldConfig> {

    /**
     * 根据表名称查询
     *
     * @param tableName 表名称
     * @return 字段配置信息
     */
    @Select("SELECT * FROM gen_field_config WHERE table_name = #{tableName} ORDER BY field_sort ASC")
    List<GenFieldConfig> selectListByTableName(@Param("tableName") String tableName);
}
