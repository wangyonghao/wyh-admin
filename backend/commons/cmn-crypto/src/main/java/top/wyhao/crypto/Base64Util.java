package top.wyhao.crypto;

import top.wyhao.crypto.exception.CryptoException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编码工具
 *
 * <p>提供标准 Base64 和 URL Safe Base64 的编解码
 *
 * <p>特性：
 * <ul>
 *     <li>基于 JDK java.util.Base64 实现</li>
 *     <li>标准 Base64：使用 +、/ 字符</li>
 *     <li>URL Safe Base64：使用 -、_ 字符，无填充</li>
 *     <li>Base64 不是加密算法，仅用于二进制传输编码</li>
 *     <li>无状态、线程安全</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public final class Base64Util {

    /**
     * 私有构造方法
     */
    private Base64Util() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 编码字节数组为标准 Base64 字符串
     *
     * @param data 待编码的字节数组
     * @return Base64 字符串
     */
    public static String encode(byte[] data) {
        if (data == null) {
            throw new CryptoException("待编码的字节数组不能为 null");
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 编码字符串为标准 Base64 字符串（使用 UTF-8）
     *
     * @param text 待编码的字符串
     * @return Base64 字符串
     */
    public static String encode(String text) {
        if (text == null) {
            throw new CryptoException("待编码的字符串不能为 null");
        }
        return encode(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码 Base64 字符串为字节数组
     *
     * @param value Base64 字符串
     * @return 解码后的字节数组
     */
    public static byte[] decode(String value) {
        if (value == null) {
            throw new CryptoException("待解码的 Base64 字符串不能为 null");
        }
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Base64 解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解码 Base64 字符串为字符串（使用 UTF-8）
     *
     * @param value Base64 字符串
     * @return 解码后的字符串
     */
    public static String decodeToString(String value) {
        byte[] bytes = decode(value);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 编码字节数组为 URL Safe Base64 字符串（无填充）
     *
     * @param data 待编码的字节数组
     * @return URL Safe Base64 字符串
     */
    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            throw new CryptoException("待编码的字节数组不能为 null");
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * 编码字符串为 URL Safe Base64 字符串（使用 UTF-8，无填充）
     *
     * @param text 待编码的字符串
     * @return URL Safe Base64 字符串
     */
    public static String encodeUrlSafe(String text) {
        if (text == null) {
            throw new CryptoException("待编码的字符串不能为 null");
        }
        return encodeUrlSafe(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码 URL Safe Base64 字符串为字节数组
     *
     * @param value URL Safe Base64 字符串
     * @return 解码后的字节数组
     */
    public static byte[] decodeUrlSafe(String value) {
        if (value == null) {
            throw new CryptoException("待解码的 URL Safe Base64 字符串不能为 null");
        }
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("URL Safe Base64 解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解码 URL Safe Base64 字符串为字符串（使用 UTF-8）
     *
     * @param value URL Safe Base64 字符串
     * @return 解码后的字符串
     */
    public static String decodeUrlSafeToString(String value) {
        byte[] bytes = decodeUrlSafe(value);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
