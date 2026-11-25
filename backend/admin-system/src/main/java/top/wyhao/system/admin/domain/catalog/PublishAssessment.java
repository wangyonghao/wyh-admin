package top.wyhao.system.admin.domain.catalog;

import java.util.List;

/**
 * 一次菜单发布的改动说明。角色引用需要调用方再次确认后才落库。
 */
public record PublishAssessment(
        List<String> changes,
        List<String> softDeleteIds,
        List<String> unlinkMenuIds,
        List<String> affectedRoleNames) {

    public boolean confirmRequired() {
        return affectedRoleNames != null && !affectedRoleNames.isEmpty();
    }
}
