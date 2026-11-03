package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.security.client.MenuApi;
import top.wyhao.system.admin.app.service.MenuService;

import java.util.List;

/**
 * 菜单 API 实现
 */
@Service
@RequiredArgsConstructor
public class MenuApiImpl implements MenuApi {

    private final MenuService menuService;

    @Override
    public List<?> getMenuTreeByUserId(Long userId) {
        return menuService.getMenuTreeByUserId(userId);
    }
}
