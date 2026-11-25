package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageGrantDO;

@Mapper
public interface TenantPackageGrantMapper extends BaseMapper<TenantPackageGrantDO> {
}
