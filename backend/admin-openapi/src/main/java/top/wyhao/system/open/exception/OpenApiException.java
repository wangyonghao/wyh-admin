package top.wyhao.system.open.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 能力开放业务异常
 */
public class OpenApiException extends BizException {

    public OpenApiException(String message) {
        super(message);
    }

    public OpenApiException(String code, String message) {
        super(code, message);
    }

    public static OpenApiException of(String message) {
        return new OpenApiException(message);
    }

    public static OpenApiException of(String code, String message) {
        return new OpenApiException(code, message);
    }

    public static OpenApiException signParamMissing(String paramName) {
        return of("OPEN_API_SIGN_PARAM_MISSING", StrUtil.format("{}不能为空", paramName));
    }

    public static OpenApiException accessKeyInvalid() {
        return of("OPEN_API_ACCESS_KEY_INVALID", "accessKey无效");
    }

    public static OpenApiException appDisabled() {
        return of("OPEN_API_APP_DISABLED", "应用已被禁用, 请联系管理员");
    }

    public static OpenApiException appExpired() {
        return of("OPEN_API_APP_EXPIRED", "应用已过期, 请联系管理员");
    }

    public static OpenApiException secretKeyMissing() {
        return of("OPEN_API_SECRET_KEY_MISSING", "秘钥缺失, 请检查应用配置");
    }
}
