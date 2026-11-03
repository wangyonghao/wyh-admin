package top.wyhao.system.admin.app.service;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.OnlineUserResult;
import top.wyhao.web.core.model.PageQuery;

import java.util.List;

/**
 * 在线会话（Token）管理
 */
public interface OnlineSessionService {

    /**
     * 分页查询在线用户。keyword 按用户名/IP 模糊过滤；total 为过滤后真实总数。
     */
    PageResult<OnlineUserResult> page(String keyword, PageQuery pageQuery);

    /**
     * 强退指定 Token（不可强退当前会话）
     */
    void revoke(String token);

    /**
     * 批量强退（跳过当前会话）
     */
    void revokeBatch(List<String> tokens);
}
