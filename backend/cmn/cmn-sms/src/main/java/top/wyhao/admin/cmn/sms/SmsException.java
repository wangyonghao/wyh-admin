package top.wyhao.admin.cmn.sms;

/**
 * 短信异常
 *

 * @since 2026/5/18
 */
public class SmsException extends RuntimeException {

    public SmsException(String message) {
        super(message);
    }

    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 短信配置未找到
     */
    public static SmsException configNotFound() {
        return new SmsException("短信配置未找到，请先配置短信服务");
    }

    /**
     * 短信配置未启用
     */
    public static SmsException configDisabled() {
        return new SmsException("短信配置未启用，请先启用短信服务");
    }

    /**
     * 短信发送失败
     */
    public static SmsException sendingFailed(String message) {
        return new SmsException("短信发送失败：" + message);
    }

    /**
     * 短信发送失败
     */
    public static SmsException sendingFailed(Exception e) {
        return new SmsException("短信发送失败", e);
    }

    /**
     * 接收人为空
     */
    public static SmsException recipientIsRequired() {
        return new SmsException("请至少指定一个接收人");
    }

    /**
     * 短信内容为空
     */
    public static SmsException contentIsRequired() {
        return new SmsException("短信内容不能为空");
    }

    /**
     * 短信模板 ID 为空
     */
    public static SmsException templateIdIsRequired() {
        return new SmsException("短信模板 ID 不能为空");
    }

    /**
     * 短信供应商配置错误
     */
    public static SmsException supplierConfigError(String supplier) {
        return new SmsException("短信供应商 [" + supplier + "] 配置错误");
    }
}
