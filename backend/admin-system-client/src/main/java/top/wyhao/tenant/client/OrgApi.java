package top.wyhao.tenant.client;

/**
 * 组织/租户会话相关窄接口（Admin 实现）。
 */
public interface OrgApi {

    /**
     * 登录后解析应写入会话的组织 ID（主组织；平台超管无组织时回落到 defaultTenantId）。
     */
    Long resolveLoginTenantId(Long userId);
}
