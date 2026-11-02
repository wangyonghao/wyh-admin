package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.OrgApplication;

/**
 * 组织申请仓储。
 */
public interface OrgApplicationRepository {

    void insert(OrgApplication application);

    void updateById(OrgApplication application);

    OrgApplication findById(Long id);

    boolean existsPending(Long tenantId, Long userId);
}
