package top.wyhao.crypto;

import top.wyhao.crypto.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES-GCM 对称加密工具
 *
 * <p>算法：AES/GCM/NoPadding
 *
 * <p>参数：
 * <ul>
 *     <li>Key: 16 / 24 / 32 bytes（128 / 192 / 256 bits）</li>
 *     <li>IV: 12 bytes（推荐）</li>
 *     <li>Authentication Tag: 128 bits</li>
 * </ul>
 *
 * <p>密文格式：IV + Ciphertext + Authentication Tag
 *
 * <p>约束：
 * <ul>
 *     <li>每次加密必须生成新的随机 IV</li>
 *     <li>使用 SecureRandom 生成 IV</li>
 *     <li>禁止 AES/ECB</li>
 *     <li>不允许复用相同 Key + IV</li>
 *     <li>不负责 Key 的生成、保存和管理</li>
 *     <li>无状态、线程安全</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public final class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // GCM 推荐 96 bits (12 bytes)
    private static final int TAG_LENGTH = 128; // Authentication Tag 长度（bits）

    /**
     * 私有构造方法
     */
    private AesUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 加密 ====================

    /**
     * AES-GCM 加密（字符串）
     *
     * @param plaintext 明文
     * @param key       密钥（16/24/32 字节）
     * @return 密文（Base64 编码，格式：IV + Ciphertext + Tag）
     */
    public static String encrypt(String plaintext, byte[] key) {
        if (plaintext == null) {
            throw new CryptoException("明文不能为 null");
        }
        byte[] encrypted = encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key);
        return Base64Util.encode(encrypted);
    }

    /**
     * AES-GCM 加密（字节数组）
     *
     * @param plaintext 明文字节数组
     * @param key       密钥（16/24/32 字节）
     * @return 密文字节数组（格式：IV + Ciphertext + Tag）
     */
    public static byte[] encrypt(byte[] plaintext, byte[] key) {
        if (plaintext == null) {
            throw new CryptoException("明文不能为 null");
        }
        validateKey(key);

        try {
            // 生成随机 IV
            byte[] iv = generateIV();

            // 创建密钥
            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);

            // 初始化 Cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // 加密
            byte[] ciphertext = cipher.doFinal(plaintext);

            // 组合 IV + Ciphertext + Tag
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            return byteBuffer.array();
        } catch (Exception e) {
            throw new CryptoException("AES-GCM 加密失败", e);
        }
    }

    // ==================== 解密 ====================

    /**
     * AES-GCM 解密（字符串）
     *
     * @param ciphertext 密文（Base64 编码，格式：IV + Ciphertext + Tag）
     * @param key        密钥（16/24/32 字节）
     * @return 明文
     */
    public static String decrypt(String ciphertext, byte[] key) {
        if (ciphertext == null) {
            throw new CryptoException("密文不能为 null");
        }
        byte[] encrypted = Base64Util.decode(ciphertext);
        byte[] decrypted = decrypt(encrypted, key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES-GCM 解密（字节数组）
     *
     * @param ciphertext 密文字节数组（格式：IV + Ciphertext + Tag）
     * @param key        密钥（16/24/32 字节）
     * @return 明文字节数组
     */
    public static byte[] decrypt(byte[] ciphertext, byte[] key) {
        if (ciphertext == null) {
            throw new CryptoException("密文不能为 null");
        }
        validateKey(key);

        if (ciphertext.length < IV_LENGTH) {
            throw new CryptoException("密文格式错误：长度不足");
        }

        try {
            // 提取 IV 和密文
            ByteBuffer byteBuffer = ByteBuffer.wrap(ciphertext);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            // 创建密钥
            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);

            // 初始化 Cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // 解密
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new CryptoException("AES-GCM 解密失败", e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成随机 IV
     *
     * @return IV 字节数组（12 字节）
     */
    private static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * 验证密钥
     *
     * @param key 密钥
     */
    private static void validateKey(byte[] key) {
        if (key == null) {
            throw new CryptoException("密钥不能为 null");
        }
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new CryptoException("密钥长度必须为 16、24 或 32 字节");
        }
    }

    /**
     * 生成 AES 密钥（用于测试或密钥生成）
     *
     * @param keySize 密钥大小（128/192/256 bits）
     * @return 密钥字节数组
     */
    public static byte[] generateKey(int keySize) {
        if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw new CryptoException("密钥大小必须为 128、192 或 256 bits");
        }
        byte[] key = new byte[keySize / 8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        return key;
    }
}
