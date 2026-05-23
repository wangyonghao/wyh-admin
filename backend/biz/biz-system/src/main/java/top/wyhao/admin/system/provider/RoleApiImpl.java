
package top.wyhao.admin.system.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.service.RoleService;
import top.wyhao.starter.core.spi.RoleApi;

/**
 * 角色业务 API 实现
 * 

 * @since 2025/7/26 9:39
 */
@Service
@RequiredArgsConstructor
public class RoleApiImpl implements RoleApi {

    private final RoleService roleService;

    @Override
    public Long getIdByCode(String code) {
        return roleService.getIdByCode(code);
    }

    @Override
    public void updateUserContext(Long roleId) {
        roleService.updateUserContext(roleId);
    }
}
