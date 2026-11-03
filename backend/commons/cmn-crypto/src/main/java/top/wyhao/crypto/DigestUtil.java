package top.wyhao.crypto;

import top.wyhao.crypto.exception.CryptoException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 消息摘要工具
 *
 * <p>提供不可逆消息摘要算法
 *
 * <p>支持算法：
 * <ul>
 *     <li>MD5（仅用于兼容或非安全场景）</li>
 *     <li>SHA-1（仅用于兼容或非安全场景）</li>
 *     <li>SHA-256（推荐）</li>
 *     <li>SHA-512</li>
 * </ul>
 *
 * <p>约束：
 * <ul>
 *     <li>默认返回 lowercase Hex</li>
 *     <li>SHA-256 为默认推荐摘要算法</li>
 *     <li>MD5 / SHA-1 仅用于兼容或非安全场景</li>
 *     <li>不用于密码存储（密码请使用 BCrypt 或 Argon2）</li>
 *     <li>无状态、线程安全</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public final class DigestUtil {

    private static final int BUFFER_SIZE = 8192;

    /**
     * 私有构造方法
     */
    private DigestUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== MD5 ====================

    /**
     * MD5 摘要（字符串）
     *
     * @param text 待摘要的字符串
     * @return MD5 摘要（小写十六进制）
     */
    public static String md5(String text) {
        if (text == null) {
            throw new CryptoException("待摘要的字符串不能为 null");
        }
        return md5(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * MD5 摘要（字节数组）
     *
     * @param data 待摘要的字节数组
     * @return MD5 摘要（小写十六进制）
     */
    public static String md5(byte[] data) {
        return digest(data, "MD5");
    }

    /**
     * MD5 摘要（输入流）
     *
     * @param input 待摘要的输入流
     * @return MD5 摘要（小写十六进制）
     */
    public static String md5(InputStream input) {
        return digest(input, "MD5");
    }

    // ==================== SHA-1 ====================

    /**
     * SHA-1 摘要（字符串）
     *
     * @param text 待摘要的字符串
     * @return SHA-1 摘要（小写十六进制）
     */
    public static String sha1(String text) {
        if (text == null) {
            throw new CryptoException("待摘要的字符串不能为 null");
        }
        return sha1(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA-1 摘要（字节数组）
     *
     * @param data 待摘要的字节数组
     * @return SHA-1 摘要（小写十六进制）
     */
    public static String sha1(byte[] data) {
        return digest(data, "SHA-1");
    }

    /**
     * SHA-1 摘要（输入流）
     *
     * @param input 待摘要的输入流
     * @return SHA-1 摘要（小写十六进制）
     */
    public static String sha1(InputStream input) {
        return digest(input, "SHA-1");
    }

    // ==================== SHA-256 ====================

    /**
     * SHA-256 摘要（字符串）
     *
     * @param text 待摘要的字符串
     * @return SHA-256 摘要（小写十六进制）
     */
    public static String sha256(String text) {
        if (text == null) {
            throw new CryptoException("待摘要的字符串不能为 null");
        }
        return sha256(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA-256 摘要（字节数组）
     *
     * @param data 待摘要的字节数组
     * @return SHA-256 摘要（小写十六进制）
     */
    public static String sha256(byte[] data) {
        return digest(data, "SHA-256");
    }

    /**
     * SHA-256 摘要（输入流）
     *
     * @param input 待摘要的输入流
     * @return SHA-256 摘要（小写十六进制）
     */
    public static String sha256(InputStream input) {
        return digest(input, "SHA-256");
    }

    // ==================== SHA-512 ====================

    /**
     * SHA-512 摘要（字符串）
     *
     * @param text 待摘要的字符串
     * @return SHA-512 摘要（小写十六进制）
     */
    public static String sha512(String text) {
        if (text == null) {
            throw new CryptoException("待摘要的字符串不能为 null");
        }
        return sha512(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA-512 摘要（字节数组）
     *
     * @param data 待摘要的字节数组
     * @return SHA-512 摘要（小写十六进制）
     */
    public static String sha512(byte[] data) {
        return digest(data, "SHA-512");
    }

    /**
     * SHA-512 摘要（输入流）
     *
     * @param input 待摘要的输入流
     * @return SHA-512 摘要（小写十六进制）
     */
    public static String sha512(InputStream input) {
        return digest(input, "SHA-512");
    }

    // ==================== 通用摘要方法 ====================

    /**
     * 通用摘要方法（字符串）
     *
     * @param text      待摘要的字符串
     * @param algorithm 摘要算法（如 MD5、SHA-1、SHA-256、SHA-512）
     * @return 摘要（小写十六进制）
     */
    public static String digest(String text, String algorithm) {
        if (text == null) {
            throw new CryptoException("待摘要的字符串不能为 null");
        }
        return digest(text.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    /**
     * 通用摘要方法（字节数组）
     *
     * @param data      待摘要的字节数组
     * @param algorithm 摘要算法（如 MD5、SHA-1、SHA-256、SHA-512）
     * @return 摘要（小写十六进制）
     */
    public static String digest(byte[] data, String algorithm) {
        if (data == null) {
            throw new CryptoException("待摘要的字节数组不能为 null");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new CryptoException("摘要算法不能为空");
        }

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(data);
            return HexUtil.encode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("不支持的摘要算法: " + algorithm, e);
        }
    }

    /**
     * 通用摘要方法（输入流）
     *
     * @param input     待摘要的输入流
     * @param algorithm 摘要算法（如 MD5、SHA-1、SHA-256、SHA-512）
     * @return 摘要（小写十六进制）
     */
    public static String digest(InputStream input, String algorithm) {
        if (input == null) {
            throw new CryptoException("待摘要的输入流不能为 null");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new CryptoException("摘要算法不能为空");
        }

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] hash = md.digest();
            return HexUtil.encode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("不支持的摘要算法: " + algorithm, e);
        } catch (IOException e) {
            throw new CryptoException("读取输入流失败", e);
        }
    }
}
