package top.wyhao.security.client;

import java.util.List;

/**
 * 菜单 API（Admin 对外提供）
 */
public interface MenuApi {

    /**
     * 获取用户可见菜单树（登录态）
     */
    List<?> getMenuTreeByUserId(Long userId);
}
