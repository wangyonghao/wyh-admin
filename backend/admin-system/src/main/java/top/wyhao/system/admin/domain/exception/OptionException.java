package top.wyhao.system.admin.domain.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 选项业务异常
 */
public class OptionException extends BizException {

    public OptionException(String message) {
        super(message);
    }

    public OptionException(String code, String message) {
        super(code, message);
    }

    public static OptionException of(String message) {
        return new OptionException(message);
    }

    public static OptionException of(String code, String message) {
        return new OptionException(code, message);
    }

    public static OptionException notFound(Long id) {
        return of("OPTION_NOT_FOUND", StrUtil.format("ID为 [{}] 的选项未找到", id));
    }

    public static OptionException valueExist(String type, String value) {
        return of("OPTION_VALUE_EXIST", StrUtil.format("类型 [{}] 中值为 [{}] 的选项已存在", type, value));
    }

    public static OptionException deleteIdsEmpty() {
        return of("OPTION_DELETE_IDS_EMPTY", "请选择要删除的数据");
    }
}
