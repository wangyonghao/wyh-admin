package top.wyhao.system.admin.domain.model;

import java.util.List;

/**
 * 管理端创建用户的领域命令（纯业务字段，不含 Web/DTO）。
 */
public record CreateUserCommand(
    String username,
    String displayName,
    String rawPassword,
    String email,
    String phone,
    Integer gender,
    Long departmentId,
    Integer status,
    String description,
    Long avatar,
    List<Long> roleIds
) {
}
