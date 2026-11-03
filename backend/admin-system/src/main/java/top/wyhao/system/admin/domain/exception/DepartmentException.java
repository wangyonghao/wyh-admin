package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 部门业务异常
 */
public class DepartmentException extends BizException {

    public DepartmentException(String message) {
        super(message);
    }

    public DepartmentException(String code, String message) {
        super(code, message);
    }

    public static DepartmentException of(String message) {
        return new DepartmentException(message);
    }

    public static DepartmentException of(String code, String message) {
        return new DepartmentException(code, message);
    }

    public static DepartmentException notFound(Long id) {
        return of("DEPARTMENT_NOT_FOUND", StrUtil.format("ID为 [{}] 的部门未找到", id));
    }

    public static DepartmentException nameExist(String name) {
        return of("DEPARTMENT_NAME_EXIST", StrUtil.format("名称为 [{}] 的部门已存在", name));
    }

    public static DepartmentException parentNotFound() {
        return of("DEPARTMENT_PARENT_NOT_FOUND", "上级部门不存在");
    }

    public static DepartmentException builtinDisableNotAllowed(String name) {
        return of("DEPARTMENT_BUILTIN_DISABLE_NOT_ALLOWED", StrUtil.format("[{}] 是系统内置部门，不允许禁用", name));
    }

    public static DepartmentException builtinParentUpdateNotAllowed(String name) {
        return of("DEPARTMENT_BUILTIN_PARENT_UPDATE_NOT_ALLOWED", StrUtil.format("[{}] 是系统内置部门，不允许变更上级部门", name));
    }

    public static DepartmentException hasEnabledChildren(String name) {
        return of("DEPARTMENT_HAS_ENABLED_CHILDREN", StrUtil.format("禁用 [{}] 前，请先禁用其所有下级部门", name));
    }

    public static DepartmentException parentDisabled(String name) {
        return of("DEPARTMENT_PARENT_DISABLED", StrUtil.format("启用 [{}] 前，请先启用其所有上级部门", name));
    }

    public static DepartmentException builtinDeleteNotAllowed(String name) {
        return of("DEPARTMENT_BUILTIN_DELETE_NOT_ALLOWED", StrUtil.format("所选部门 [{}] 是系统内置部门，不允许删除", name));
    }

    public static DepartmentException hasChildren() {
        return of("DEPARTMENT_HAS_CHILDREN", "所选部门存在下级部门，不允许删除");
    }

    public static DepartmentException hasUsers() {
        return of("DEPARTMENT_HAS_USERS", "所选部门存在用户关联，请解除关联后重试");
    }

    public static DepartmentException pathBlank() {
        return of("DEPARTMENT_PATH_BLANK", "部门路径不能为空");
    }

    public static DepartmentException pathFormatInvalid(String departmentPath) {
        return of("DEPARTMENT_PATH_FORMAT_INVALID", StrUtil.format("部门路径格式无效：{}", departmentPath));
    }

    public static DepartmentException pathContainsBlank(String departmentPath) {
        return of("DEPARTMENT_PATH_CONTAINS_BLANK", StrUtil.format("部门路径包含空段：{}", departmentPath));
    }

    public static DepartmentException notFoundInPath(String name, String departmentPath) {
        return of("DEPARTMENT_NOT_FOUND_IN_PATH", StrUtil.format("路径 [{}] 中未找到部门 [{}]", departmentPath, name));
    }

    public static DepartmentException notFoundByName(String departmentName) {
        return of("DEPARTMENT_NOT_FOUND_BY_NAME", StrUtil.format("部门 [{}] 不存在", departmentName));
    }

    public static DepartmentException nameDuplicate(String departmentName) {
        return of("DEPARTMENT_NAME_DUPLICATE", StrUtil.format("存在多个同名部门 [{}]，请使用完整层级路径", departmentName));
    }
}
