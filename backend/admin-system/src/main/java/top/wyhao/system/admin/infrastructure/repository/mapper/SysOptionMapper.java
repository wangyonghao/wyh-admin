package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOptionDO;

/**
 * 字典选项 Mapper
 */
@Mapper
public interface SysOptionMapper extends BaseMapper<SysOptionDO> {
}
