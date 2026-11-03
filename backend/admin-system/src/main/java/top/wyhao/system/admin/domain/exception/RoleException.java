package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 角色业务异常
 */
public class RoleException extends BizException {

    public RoleException(String message) {
        super(message);
    }

    public RoleException(String code, String message) {
        super(code, message);
    }

    public static RoleException of(String message) {
        return new RoleException(message);
    }

    public static RoleException of(String code, String message) {
        return new RoleException(code, message);
    }

    public static RoleException notFound() {
        return of("ROLE_NOT_FOUND", "角色不存在");
    }

    public static RoleException createFailed() {
        return of("CREATE_FAILED", "创建失败");
    }

    public static RoleException updateFailed() {
        return of("UPDATE_FAILED", "更新失败");
    }

    public static RoleException nameAlreadyExists(String name) {
        return of("ROLENAME_ALREADY_EXISTS", StrUtil.format("角色名称 [{}] 已存在", name));
    }

    public static RoleException builtinNotAllowedDelete(String name) {
        return of("ROLE_NOT_ALLOWED_DELETE", StrUtil.format("所选角色 [{}] 是系统内置角色，不允许删除", name));
    }

    public static RoleException hasUserRelation() {
        return of("ROLE_NOT_ALLOWED_DELETE", "所选角色存在用户关联，请解除关联后重试");
    }

    public static RoleException codeForbidden(String code) {
        return of("ROLE_CODE_FORBIDDEN", StrUtil.format("编码 [{}] 禁止使用", code));
    }

    public static RoleException codeUpdateNotAllowed() {
        return of("ROLE_CODE_UPDATE_NOT_ALLOWED", "角色编码不允许修改");
    }

    public static RoleException builtinDataScopeUpdateNotAllowed(String name) {
        return of("ROLE_BUILTIN_DATA_SCOPE_UPDATE_NOT_ALLOWED", StrUtil.format("[{}] 是系统内置角色，不允许修改角色数据权限", name));
    }

    public static RoleException builtinPermissionUpdateNotAllowed(String name) {
        return of("ROLE_BUILTIN_PERMISSION_UPDATE_NOT_ALLOWED", StrUtil.format("[{}] 是系统内置角色，不允许修改角色功能权限", name));
    }

    public static RoleException builtinAssignNotAllowed(String name) {
        return of("ROLE_BUILTIN_ASSIGN_NOT_ALLOWED", StrUtil.format("[{}] 是系统内置角色，不允许分配角色给其他用户", name));
    }
}
