package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 套餐业务异常
 */
public class PackageException extends BizException {

    public PackageException(String message) {
        super(message);
    }

    public PackageException(String code, String message) {
        super(code, message);
    }

    public static PackageException of(String message) {
        return new PackageException(message);
    }

    public static PackageException of(String code, String message) {
        return new PackageException(code, message);
    }

    public static PackageException nameExists(String name) {
        return of("PACKAGE_NAME_EXISTS", StrUtil.format("名称为 [{}] 的套餐已存在", name));
    }

    public static PackageException notFound() {
        return of("PACKAGE_NOT_FOUND", "套餐不存在");
    }

    public static PackageException disabled() {
        return of("PACKAGE_DISABLED", "套餐已被禁用");
    }

    public static PackageException inUse() {
        return of("PACKAGE_IN_USE", "套餐仍被租户使用，不能删除");
    }
}
