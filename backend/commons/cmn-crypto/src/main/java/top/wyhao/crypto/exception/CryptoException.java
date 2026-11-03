package top.wyhao.crypto.exception;

/**
 * 加密异常
 *
 * @author wyh
 * @since 2026/09/16
 */
public class CryptoException extends RuntimeException {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
