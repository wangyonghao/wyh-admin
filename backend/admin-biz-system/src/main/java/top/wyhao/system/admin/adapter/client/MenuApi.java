package top.wyhao.system.admin.adapter.client;

import java.util.List;

/**
 * 菜单 API（授权域对外提供）
 */
public interface MenuApi {

    /**
     * 获取用户可见菜单树（登录态）
     */
    List<?> getMenuTreeByUserId(Long userId);
}
