package top.wyhao.system.admin.app.query;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.RoleMemberQuery;
import top.wyhao.system.admin.app.dto.RoleQuery;
import top.wyhao.system.admin.app.dto.RoleMemberResult;
import top.wyhao.system.admin.domain.model.Role;

import java.util.List;

/**
 * 角色列表读模型。分页 SQL 直接映射到接口视图，因此端口放在应用层。
 */
public interface RoleViewQuery {

    PageResult<Role> page(RoleQuery query, long page, long pageSize);

    List<Role> list(RoleQuery query);

    List<RoleMemberResult> pageMembers(Long roleId, RoleMemberQuery query, long page, long pageSize);
}
