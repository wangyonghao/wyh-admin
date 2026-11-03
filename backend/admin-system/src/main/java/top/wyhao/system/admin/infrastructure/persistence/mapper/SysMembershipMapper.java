package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMembershipDO;

/**
 * 成员关系 Mapper。
 */
@Mapper
public interface SysMembershipMapper extends BaseMapper<SysMembershipDO> {
}
