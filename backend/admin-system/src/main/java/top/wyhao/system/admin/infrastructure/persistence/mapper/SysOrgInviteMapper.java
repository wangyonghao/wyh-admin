package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOrgInviteDO;

@Mapper
public interface SysOrgInviteMapper extends BaseMapper<SysOrgInviteDO> {
}
