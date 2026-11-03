package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.Role;

import java.util.Collection;
import java.util.List;

/**
 * 角色仓储。
 */
public interface RoleRepository {

    Role findById(Long id);

    int insert(Role role);

    int updateById(Role role);

    void deleteById(Long id);

    boolean nameExists(String name, Long selfId);

    void updateMenuCheckStrictly(Long roleId, Boolean menuCheckStrictly);

    Long findIdByCode(String code);

    List<Role> listByNames(List<String> names);

    int countByNames(List<String> names);

    void deleteAll();

    long countExcluding(Collection<Long> keepIds);

    boolean deleteExcluding(Collection<Long> keepIds);

    List<Role> selectRolesByUserId(Long id);
}
