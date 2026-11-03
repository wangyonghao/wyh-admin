package top.wyhao.crypto;

import top.wyhao.crypto.exception.CryptoException;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA 非对称加密和数字签名工具
 *
 * <p>加密算法：RSA/ECB/OAEPWithSHA-256AndMGF1Padding
 * <p>签名算法：SHA256withRSA
 *
 * <p>密钥格式：
 * <ul>
 *     <li>公钥：X.509 / SubjectPublicKeyInfo，Base64 编码</li>
 *     <li>私钥：PKCS#8，Base64 编码</li>
 * </ul>
 *
 * <p>约束：
 * <ul>
 *     <li>RSA 不用于大数据或大文件直接加密</li>
 *     <li>大数据采用混合加密（AES-GCM + RSA-OAEP）</li>
 *     <li>不负责密钥的生成、保存和管理</li>
 *     <li>无状态、线程安全</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public final class RsaUtil {

    private static final String ALGORITHM = "RSA";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * 私有构造方法
     */
    private RsaUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 密钥解析 ====================

    /**
     * 从 Base64 字符串解析公钥
     *
     * @param base64PublicKey Base64 编码的公钥（X.509 格式）
     * @return 公钥对象
     */
    public static PublicKey publicKey(String base64PublicKey) {
        if (base64PublicKey == null || base64PublicKey.isEmpty()) {
            throw new CryptoException("公钥不能为空");
        }

        try {
            byte[] keyBytes = Base64Util.decode(base64PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new CryptoException("解析公钥失败", e);
        }
    }

    /**
     * 从 Base64 字符串解析私钥
     *
     * @param base64PrivateKey Base64 编码的私钥（PKCS#8 格式）
     * @return 私钥对象
     */
    public static PrivateKey privateKey(String base64PrivateKey) {
        if (base64PrivateKey == null || base64PrivateKey.isEmpty()) {
            throw new CryptoException("私钥不能为空");
        }

        try {
            byte[] keyBytes = Base64Util.decode(base64PrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new CryptoException("解析私钥失败", e);
        }
    }

    // ==================== 加密 ====================

    /**
     * RSA 加密（字符串）
     *
     * @param plaintext     明文
     * @param publicKeyStr  公钥（Base64 编码）
     * @return 密文（Base64 编码）
     */
    public static String encrypt(String plaintext, String publicKeyStr) {
        if (plaintext == null) {
            throw new CryptoException("明文不能为 null");
        }
        PublicKey publicKey = publicKey(publicKeyStr);
        byte[] encrypted = encrypt(plaintext.getBytes(StandardCharsets.UTF_8), publicKey);
        return Base64Util.encode(encrypted);
    }

    /**
     * RSA 加密（字节数组）
     *
     * @param data      待加密数据
     * @param publicKey 公钥对象
     * @return 密文字节数组
     */
    public static byte[] encrypt(byte[] data, PublicKey publicKey) {
        if (data == null) {
            throw new CryptoException("待加密数据不能为 null");
        }
        if (publicKey == null) {
            throw new CryptoException("公钥不能为 null");
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("RSA 加密失败", e);
        }
    }

    // ==================== 解密 ====================

    /**
     * RSA 解密（字符串）
     *
     * @param ciphertext     密文（Base64 编码）
     * @param privateKeyStr  私钥（Base64 编码）
     * @return 明文
     */
    public static String decrypt(String ciphertext, String privateKeyStr) {
        if (ciphertext == null) {
            throw new CryptoException("密文不能为 null");
        }
        PrivateKey privateKey = privateKey(privateKeyStr);
        byte[] encryptedData = Base64Util.decode(ciphertext);
        byte[] decrypted = decrypt(encryptedData, privateKey);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * RSA 解密（字节数组）
     *
     * @param data       密文字节数组
     * @param privateKey 私钥对象
     * @return 明文字节数组
     */
    public static byte[] decrypt(byte[] data, PrivateKey privateKey) {
        if (data == null) {
            throw new CryptoException("密文不能为 null");
        }
        if (privateKey == null) {
            throw new CryptoException("私钥不能为 null");
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("RSA 解密失败", e);
        }
    }

    // ==================== 签名 ====================

    /**
     * RSA 签名（字符串）
     *
     * @param data          待签名数据
     * @param privateKeyStr 私钥（Base64 编码）
     * @return 签名（Base64 编码）
     */
    public static String sign(String data, String privateKeyStr) {
        if (data == null) {
            throw new CryptoException("待签名数据不能为 null");
        }
        PrivateKey privateKey = privateKey(privateKeyStr);
        byte[] signature = sign(data.getBytes(StandardCharsets.UTF_8), privateKey);
        return Base64Util.encode(signature);
    }

    /**
     * RSA 签名（字节数组）
     *
     * @param data       待签名数据
     * @param privateKey 私钥对象
     * @return 签名字节数组
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey) {
        if (data == null) {
            throw new CryptoException("待签名数据不能为 null");
        }
        if (privateKey == null) {
            throw new CryptoException("私钥不能为 null");
        }

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new CryptoException("RSA 签名失败", e);
        }
    }

    // ==================== 验签 ====================

    /**
     * RSA 验签（字符串）
     *
     * @param data         原始数据
     * @param signatureStr 签名（Base64 编码）
     * @param publicKeyStr 公钥（Base64 编码）
     * @return 验签是否通过
     */
    public static boolean verify(String data, String signatureStr, String publicKeyStr) {
        if (data == null) {
            throw new CryptoException("原始数据不能为 null");
        }
        if (signatureStr == null || signatureStr.isEmpty()) {
            return false;
        }
        PublicKey publicKey = publicKey(publicKeyStr);
        byte[] signatureBytes = Base64Util.decode(signatureStr);
        return verify(data.getBytes(StandardCharsets.UTF_8), signatureBytes, publicKey);
    }

    /**
     * RSA 验签（字节数组）
     *
     * @param data      原始数据
     * @param signature 签名字节数组
     * @param publicKey 公钥对象
     * @return 验签是否通过
     */
    public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        if (data == null) {
            throw new CryptoException("原始数据不能为 null");
        }
        if (signature == null || signature.length == 0) {
            return false;
        }
        if (publicKey == null) {
            throw new CryptoException("公钥不能为 null");
        }

        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            throw new CryptoException("RSA 验签失败", e);
        }
    }

    // ==================== 密钥对生成 ====================

    /**
     * 生成 RSA 密钥对（用于测试或密钥生成）
     *
     * @param keySize 密钥大小（推荐 2048 或 4096）
     * @return 密钥对
     */
    public static KeyPair generateKeyPair(int keySize) {
        if (keySize < 2048) {
            throw new CryptoException("RSA 密钥大小至少为 2048 bits");
        }

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(keySize);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new CryptoException("生成 RSA 密钥对失败", e);
        }
    }

    /**
     * 将公钥转换为 Base64 字符串
     *
     * @param publicKey 公钥对象
     * @return Base64 编码的公钥
     */
    public static String publicKeyToBase64(PublicKey publicKey) {
        if (publicKey == null) {
            throw new CryptoException("公钥不能为 null");
        }
        return Base64Util.encode(publicKey.getEncoded());
    }

    /**
     * 将私钥转换为 Base64 字符串
     *
     * @param privateKey 私钥对象
     * @return Base64 编码的私钥
     */
    public static String privateKeyToBase64(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new CryptoException("私钥不能为 null");
        }
        return Base64Util.encode(privateKey.getEncoded());
    }
}
