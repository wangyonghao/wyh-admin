package top.wyhao.crypto;

import top.wyhao.crypto.exception.CryptoException;

/**
 * 十六进制编码工具
 *
 * <p>提供 byte[] 与 Hex 字符串的双向转换
 *
 * <p>特性：
 * <ul>
 *     <li>默认 lowercase Hex</li>
 *     <li>不承担加密或摘要职责</li>
 *     <li>无状态、线程安全</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public final class HexUtil {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 私有构造方法
     */
    private HexUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将字节数组编码为十六进制字符串（小写）
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String encode(byte[] bytes) {
        if (bytes == null) {
            throw new CryptoException("待编码的字节数组不能为 null");
        }

        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 将十六进制字符串解码为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] decode(String hex) {
        if (hex == null) {
            throw new CryptoException("待解码的十六进制字符串不能为 null");
        }

        int len = hex.length();
        if (len % 2 != 0) {
            throw new CryptoException("十六进制字符串长度必须为偶数");
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
