package top.wyhao.system.admin.domain.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 通知投递业务异常（邮件 / 短信）
 */
public class NotifyException extends BizException {

    public NotifyException(String code, String message) {
        super(code, message);
    }

    public static NotifyException of(String code, String message) {
        return new NotifyException(code, message);
    }

    public static NotifyException channelDisabled(String channel) {
        return of("NOTIFY_CHANNEL_DISABLED", channel + "渠道未启用");
    }

    public static NotifyException sendFailed(String message) {
        return of("NOTIFY_SEND_FAILED", message);
    }
}
