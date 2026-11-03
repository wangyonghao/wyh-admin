package top.wyhao.system.admin.domain.model;

import cn.hutool.core.collection.CollUtil;
import top.wyhao.cmn.core.enums.DataScopeEnum;

import java.util.Collections;
import java.util.List;

/**
 * 登录会话里，一个角色能看见哪些部门。
 *
 * <p>全部数据和仅本人不按部门收窄，返回 {@code null}。部门类范围返回具体 ID，没有部门时返回空列表。
 */
public final class RoleDataScopeRules {

    private RoleDataScopeRules() {
    }

    public static List<Long> visibleDepartmentIds(DataScopeEnum dataScope,
                                                   Long userDepartmentId,
                                                   List<Long> departmentAndDescendants,
                                                   List<Long> customDepartmentIds) {
        if (dataScope == null) {
            return null;
        }
        return switch (dataScope) {
            case DEPARTMENT_AND_CHILD -> CollUtil.emptyIfNull(departmentAndDescendants);
            case DEPARTMENT -> userDepartmentId == null ? Collections.emptyList() : List.of(userDepartmentId);
            case CUSTOM_DEPARTMENT -> customDepartmentIds;
            case ALL, SELF -> null;
        };
    }
}
