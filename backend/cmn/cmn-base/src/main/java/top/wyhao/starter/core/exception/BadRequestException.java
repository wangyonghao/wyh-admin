
package top.wyhao.starter.core.exception;

/**
 * 自定义验证异常-错误请求
 *

 * @since 1.0.0
 */
public class BadRequestException extends BusinessException {
    public BadRequestException(String code, Object[] args) {
        super(code, args);
    }

    public BadRequestException(String code, String defaultMessage) {
        super(code, defaultMessage);
    }

    public BadRequestException(String code, Object[] args, String defaultMessage) {
        super(code, args, defaultMessage);
    }
}
