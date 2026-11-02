package top.wyhao.system.admin.domain.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 短信业务异常
 */
public class SmsException extends BizException {

    public SmsException(String message) {
        super(message);
    }

    public SmsException(String code, String message) {
        super(code, message);
    }

    public static SmsException of(String message) {
        return new SmsException(message);
    }

    public static SmsException of(String code, String message) {
        return new SmsException(code, message);
    }

    public static SmsException notFound() {
        return of("SMS_LOG_NOT_FOUND", "记录不存在");
    }
}
