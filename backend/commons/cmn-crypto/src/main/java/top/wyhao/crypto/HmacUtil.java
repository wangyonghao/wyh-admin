package top.wyhao.crypto;

import top.wyhao.crypto.exception.CryptoException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC 工具
 *
 * <p>提供基于密钥的消息认证码（Hash-based Message Authentication Code）
 *
 * <p>支持算法：
 * <ul>
 *     <li>HmacSHA256（推荐）</li>
 *     <li>HmacSHA512</li>
 * </ul>
 *
 * <p>约束：
 * <ul>
 *     <li>返回 lowercase Hex</li>
 *     <li>Secret 不得硬编码</li>
 *     <li>Secret 不由工具类保存</li>
 *     <li>用于 API 签名、Webhook 签名、服务间认证等</li>
 *     <li>无状态、线程安全</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public final class HmacUtil {

    /**
     * 私有构造方法
     */
    private HmacUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== HmacSHA256 ====================

    /**
     * HmacSHA256 签名（字符串）
     *
     * @param data   待签名的数据
     * @param secret 密钥
     * @return HMAC 签名（小写十六进制）
     */
    public static String hmacSha256(String data, String secret) {
        if (data == null) {
            throw new CryptoException("待签名的数据不能为 null");
        }
        if (secret == null) {
            throw new CryptoException("密钥不能为 null");
        }
        return hmacSha256(
                data.getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * HmacSHA256 签名（字节数组）
     *
     * @param data   待签名的数据
     * @param secret 密钥
     * @return HMAC 签名（小写十六进制）
     */
    public static String hmacSha256(byte[] data, byte[] secret) {
        return hmac(data, secret, "HmacSHA256");
    }

    // ==================== HmacSHA512 ====================

    /**
     * HmacSHA512 签名（字符串）
     *
     * @param data   待签名的数据
     * @param secret 密钥
     * @return HMAC 签名（小写十六进制）
     */
    public static String hmacSha512(String data, String secret) {
        if (data == null) {
            throw new CryptoException("待签名的数据不能为 null");
        }
        if (secret == null) {
            throw new CryptoException("密钥不能为 null");
        }
        return hmacSha512(
                data.getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * HmacSHA512 签名（字节数组）
     *
     * @param data   待签名的数据
     * @param secret 密钥
     * @return HMAC 签名（小写十六进制）
     */
    public static String hmacSha512(byte[] data, byte[] secret) {
        return hmac(data, secret, "HmacSHA512");
    }

    // ==================== 通用 HMAC 方法 ====================

    /**
     * 通用 HMAC 签名方法（字符串）
     *
     * @param data      待签名的数据
     * @param secret    密钥
     * @param algorithm 算法（如 HmacSHA256、HmacSHA512）
     * @return HMAC 签名（小写十六进制）
     */
    public static String hmac(String data, String secret, String algorithm) {
        if (data == null) {
            throw new CryptoException("待签名的数据不能为 null");
        }
        if (secret == null) {
            throw new CryptoException("密钥不能为 null");
        }
        return hmac(
                data.getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8),
                algorithm
        );
    }

    /**
     * 通用 HMAC 签名方法（字节数组）
     *
     * @param data      待签名的数据
     * @param secret    密钥
     * @param algorithm 算法（如 HmacSHA256、HmacSHA512）
     * @return HMAC 签名（小写十六进制）
     */
    public static String hmac(byte[] data, byte[] secret, String algorithm) {
        if (data == null) {
            throw new CryptoException("待签名的数据不能为 null");
        }
        if (secret == null || secret.length == 0) {
            throw new CryptoException("密钥不能为空");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new CryptoException("HMAC 算法不能为空");
        }

        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret, algorithm);
            mac.init(secretKeySpec);
            byte[] hmac = mac.doFinal(data);
            return HexUtil.encode(hmac);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("不支持的 HMAC 算法: " + algorithm, e);
        } catch (InvalidKeyException e) {
            throw new CryptoException("无效的密钥", e);
        }
    }

    /**
     * 验证 HMAC 签名（防止时序攻击）
     *
     * @param data      原始数据
     * @param secret    密钥
     * @param signature 待验证的签名
     * @param algorithm 算法（如 HmacSHA256、HmacSHA512）
     * @return 签名是否匹配
     */
    public static boolean verify(String data, String secret, String signature, String algorithm) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        String computed = hmac(data, secret, algorithm);
        return constantTimeEquals(computed, signature);
    }

    /**
     * 常量时间字符串比较（防止时序攻击）
     *
     * @param a 字符串 A
     * @param b 字符串 B
     * @return 是否相等
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
