package top.wyhao.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import top.wyhao.crypto.exception.CryptoException;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RsaUtil 单元测试
 *
 * @author wyh
 * @since 2026/09/16
 */
class RsaUtilTest {

    private static String publicKeyBase64;
    private static String privateKeyBase64;
    private static final String TEST_PLAINTEXT = "Hello, World!";

    @BeforeAll
    static void setUp() {
        // 生成测试用的密钥对
        KeyPair keyPair = RsaUtil.generateKeyPair(2048);
        publicKeyBase64 = RsaUtil.publicKeyToBase64(keyPair.getPublic());
        privateKeyBase64 = RsaUtil.privateKeyToBase64(keyPair.getPrivate());
    }

    @Test
    void testGenerateKeyPair() {
        // 测试生成密钥对
        KeyPair keyPair = RsaUtil.generateKeyPair(2048);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }

    @Test
    void testGenerateKeyPairInvalidSize() {
        // 测试无效的密钥大小
        assertThrows(CryptoException.class, () -> RsaUtil.generateKeyPair(1024));
    }

    @Test
    void testPublicKeyConversion() {
        // 测试公钥转换
        PublicKey publicKey = RsaUtil.publicKey(publicKeyBase64);
        assertNotNull(publicKey);
        
        String base64 = RsaUtil.publicKeyToBase64(publicKey);
        assertEquals(publicKeyBase64, base64);
    }

    @Test
    void testPrivateKeyConversion() {
        // 测试私钥转换
        PrivateKey privateKey = RsaUtil.privateKey(privateKeyBase64);
        assertNotNull(privateKey);
        
        String base64 = RsaUtil.privateKeyToBase64(privateKey);
        assertEquals(privateKeyBase64, base64);
    }

    @Test
    void testPublicKeyNull() {
        // 测试 null 公钥
        assertThrows(CryptoException.class, () -> RsaUtil.publicKey(null));
        assertThrows(CryptoException.class, () -> RsaUtil.publicKey(""));
        assertThrows(CryptoException.class, () -> RsaUtil.publicKeyToBase64(null));
    }

    @Test
    void testPrivateKeyNull() {
        // 测试 null 私钥
        assertThrows(CryptoException.class, () -> RsaUtil.privateKey(null));
        assertThrows(CryptoException.class, () -> RsaUtil.privateKey(""));
        assertThrows(CryptoException.class, () -> RsaUtil.privateKeyToBase64(null));
    }

    @Test
    void testRsaEncryptDecryptString() {
        // 测试字符串加密解密
        String ciphertext = RsaUtil.encrypt(TEST_PLAINTEXT, publicKeyBase64);
        assertNotNull(ciphertext);
        assertTrue(ciphertext.length() > 0);
        
        String decrypted = RsaUtil.decrypt(ciphertext, privateKeyBase64);
        assertEquals(TEST_PLAINTEXT, decrypted);
    }

    @Test
    void testRsaEncryptDecryptBytes() {
        // 测试字节数组加密解密
        PublicKey publicKey = RsaUtil.publicKey(publicKeyBase64);
        PrivateKey privateKey = RsaUtil.privateKey(privateKeyBase64);
        
        byte[] plaintext = TEST_PLAINTEXT.getBytes();
        byte[] ciphertext = RsaUtil.encrypt(plaintext, publicKey);
        assertNotNull(ciphertext);
        assertTrue(ciphertext.length > 0);
        
        byte[] decrypted = RsaUtil.decrypt(ciphertext, privateKey);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void testRsaEncryptNullPlaintext() {
        // 测试 null 明文
        assertThrows(CryptoException.class, () -> RsaUtil.encrypt((String) null, publicKeyBase64));
        assertThrows(CryptoException.class, () -> RsaUtil.encrypt((byte[]) null, RsaUtil.publicKey(publicKeyBase64)));
    }

    @Test
    void testRsaEncryptNullKey() {
        // 测试 null 密钥
        assertThrows(CryptoException.class, () -> RsaUtil.encrypt(TEST_PLAINTEXT, (String) null));
        assertThrows(CryptoException.class, () -> RsaUtil.encrypt(TEST_PLAINTEXT.getBytes(), (PublicKey) null));
    }

    @Test
    void testRsaDecryptNullCiphertext() {
        // 测试 null 密文
        assertThrows(CryptoException.class, () -> RsaUtil.decrypt((String) null, privateKeyBase64));
        assertThrows(CryptoException.class, () -> RsaUtil.decrypt((byte[]) null, RsaUtil.privateKey(privateKeyBase64)));
    }

    @Test
    void testRsaDecryptNullKey() {
        // 测试 null 密钥
        String ciphertext = RsaUtil.encrypt(TEST_PLAINTEXT, publicKeyBase64);
        assertThrows(CryptoException.class, () -> RsaUtil.decrypt(ciphertext, (String) null));
        assertThrows(CryptoException.class, () -> RsaUtil.decrypt(Base64Util.decode(ciphertext), (PrivateKey) null));
    }

    @Test
    void testRsaDecryptWithWrongKey() {
        // 测试使用错误的密钥解密
        KeyPair anotherKeyPair = RsaUtil.generateKeyPair(2048);
        String anotherPrivateKey = RsaUtil.privateKeyToBase64(anotherKeyPair.getPrivate());
        
        String ciphertext = RsaUtil.encrypt(TEST_PLAINTEXT, publicKeyBase64);
        assertThrows(CryptoException.class, () -> RsaUtil.decrypt(ciphertext, anotherPrivateKey));
    }

    @Test
    void testRsaSignVerifyString() {
        // 测试字符串签名验证
        String signature = RsaUtil.sign(TEST_PLAINTEXT, privateKeyBase64);
        assertNotNull(signature);
        assertTrue(signature.length() > 0);
        
        boolean valid = RsaUtil.verify(TEST_PLAINTEXT, signature, publicKeyBase64);
        assertTrue(valid);
    }

    @Test
    void testRsaSignVerifyBytes() {
        // 测试字节数组签名验证
        PrivateKey privateKey = RsaUtil.privateKey(privateKeyBase64);
        PublicKey publicKey = RsaUtil.publicKey(publicKeyBase64);
        
        byte[] data = TEST_PLAINTEXT.getBytes();
        byte[] signature = RsaUtil.sign(data, privateKey);
        assertNotNull(signature);
        assertTrue(signature.length > 0);
        
        boolean valid = RsaUtil.verify(data, signature, publicKey);
        assertTrue(valid);
    }

    @Test
    void testRsaVerifyInvalidSignature() {
        // 非法 Base64 / 损坏签名：实现抛 CryptoException，而非静默 false
        assertThrows(CryptoException.class,
                () -> RsaUtil.verify(TEST_PLAINTEXT, "invalidsignature", publicKeyBase64));

        // null / 空签名视为不通过
        assertFalse(RsaUtil.verify(TEST_PLAINTEXT, null, publicKeyBase64));
        assertFalse(RsaUtil.verify(TEST_PLAINTEXT, "", publicKeyBase64));
    }

    @Test
    void testRsaVerifyModifiedData() {
        // 测试修改数据后验证失败
        String signature = RsaUtil.sign(TEST_PLAINTEXT, privateKeyBase64);
        assertFalse(RsaUtil.verify("Modified Data", signature, publicKeyBase64));
    }

    @Test
    void testRsaVerifyWithWrongKey() {
        // 测试使用错误的公钥验证
        KeyPair anotherKeyPair = RsaUtil.generateKeyPair(2048);
        String anotherPublicKey = RsaUtil.publicKeyToBase64(anotherKeyPair.getPublic());
        
        String signature = RsaUtil.sign(TEST_PLAINTEXT, privateKeyBase64);
        assertFalse(RsaUtil.verify(TEST_PLAINTEXT, signature, anotherPublicKey));
    }

    @Test
    void testRsaSignNullData() {
        // 测试 null 数据签名
        assertThrows(CryptoException.class, () -> RsaUtil.sign((String) null, privateKeyBase64));
        assertThrows(CryptoException.class, () -> RsaUtil.sign((byte[]) null, RsaUtil.privateKey(privateKeyBase64)));
    }

    @Test
    void testRsaSignNullKey() {
        // 测试 null 密钥签名
        assertThrows(CryptoException.class, () -> RsaUtil.sign(TEST_PLAINTEXT, (String) null));
        assertThrows(CryptoException.class, () -> RsaUtil.sign(TEST_PLAINTEXT.getBytes(), (PrivateKey) null));
    }

    @Test
    void testRsaVerifyNullData() {
        // 测试 null 数据验证
        String signature = RsaUtil.sign(TEST_PLAINTEXT, privateKeyBase64);
        assertThrows(CryptoException.class, () -> RsaUtil.verify((String) null, signature, publicKeyBase64));
        assertThrows(CryptoException.class, () -> RsaUtil.verify((byte[]) null, Base64Util.decode(signature), RsaUtil.publicKey(publicKeyBase64)));
    }

    @Test
    void testRsaVerifyNullKey() {
        // 测试 null 密钥验证
        String signature = RsaUtil.sign(TEST_PLAINTEXT, privateKeyBase64);
        assertThrows(CryptoException.class, () -> RsaUtil.verify(TEST_PLAINTEXT, signature, (String) null));
        assertThrows(CryptoException.class, () -> RsaUtil.verify(TEST_PLAINTEXT.getBytes(), Base64Util.decode(signature), (PublicKey) null));
    }

    @Test
    void testRsaChineseText() {
        // 测试中文文本加密和签名
        String chineseText = "你好，世界！这是一个测试。";
        
        // 加密解密
        String ciphertext = RsaUtil.encrypt(chineseText, publicKeyBase64);
        String decrypted = RsaUtil.decrypt(ciphertext, privateKeyBase64);
        assertEquals(chineseText, decrypted);
        
        // 签名验证
        String signature = RsaUtil.sign(chineseText, privateKeyBase64);
        assertTrue(RsaUtil.verify(chineseText, signature, publicKeyBase64));
    }

    @Test
    void testRsaDifferentPlaintextsDifferentCiphertexts() {
        // 测试不同明文产生不同密文
        String ciphertext1 = RsaUtil.encrypt("Hello", publicKeyBase64);
        String ciphertext2 = RsaUtil.encrypt("World", publicKeyBase64);
        assertNotEquals(ciphertext1, ciphertext2);
    }

    @Test
    void testRsaSamePlaintextDifferentCiphertexts() {
        // 测试相同明文每次加密产生不同密文（因为使用了随机填充）
        String ciphertext1 = RsaUtil.encrypt(TEST_PLAINTEXT, publicKeyBase64);
        String ciphertext2 = RsaUtil.encrypt(TEST_PLAINTEXT, publicKeyBase64);
        assertNotEquals(ciphertext1, ciphertext2);
        
        // 但解密后应该相同
        String decrypted1 = RsaUtil.decrypt(ciphertext1, privateKeyBase64);
        String decrypted2 = RsaUtil.decrypt(ciphertext2, privateKeyBase64);
        assertEquals(decrypted1, decrypted2);
        assertEquals(TEST_PLAINTEXT, decrypted1);
    }
}
