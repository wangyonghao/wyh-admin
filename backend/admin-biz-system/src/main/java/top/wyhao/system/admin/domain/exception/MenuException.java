package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 菜单业务异常
 */
public class MenuException extends BizException {

    public MenuException(String message) {
        super(message);
    }

    public MenuException(String code, String message) {
        super(code, message);
    }

    public static MenuException of(String message) {
        return new MenuException(message);
    }

    public static MenuException of(String code, String message) {
        return new MenuException(code, message);
    }

    public static MenuException titleExist(String name) {
        return of("TITLE_EXIST", StrUtil.format("标题为 [{}] 的菜单已存在", name));
    }

    public static MenuException notFound() {
        return of("MENU_NOT_FOUND", "菜单不存在");
    }
}
