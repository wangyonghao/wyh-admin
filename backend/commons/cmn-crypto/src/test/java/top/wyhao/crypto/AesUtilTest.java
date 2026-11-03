package top.wyhao.crypto;

import org.junit.jupiter.api.Test;
import top.wyhao.crypto.exception.CryptoException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AesUtil 单元测试
 *
 * @author wyh
 * @since 2026/09/16
 */
class AesUtilTest {

    private static final String TEST_PLAINTEXT = "Hello, World!";

    @Test
    void testGenerateKey() {
        // 测试生成 128 位密钥
        byte[] key128 = AesUtil.generateKey(128);
        assertEquals(16, key128.length);

        // 测试生成 192 位密钥
        byte[] key192 = AesUtil.generateKey(192);
        assertEquals(24, key192.length);

        // 测试生成 256 位密钥
        byte[] key256 = AesUtil.generateKey(256);
        assertEquals(32, key256.length);
    }

    @Test
    void testGenerateKeyInvalidSize() {
        // 测试无效的密钥大小
        assertThrows(CryptoException.class, () -> AesUtil.generateKey(64));
        assertThrows(CryptoException.class, () -> AesUtil.generateKey(512));
    }

    @Test
    void testEncryptDecryptString() {
        // 测试字符串加密解密
        byte[] key = AesUtil.generateKey(256);
        
        String ciphertext = AesUtil.encrypt(TEST_PLAINTEXT, key);
        assertNotNull(ciphertext);
        assertTrue(ciphertext.length() > 0);
        
        String decrypted = AesUtil.decrypt(ciphertext, key);
        assertEquals(TEST_PLAINTEXT, decrypted);
    }

    @Test
    void testEncryptDecryptBytes() {
        // 测试字节数组加密解密
        byte[] key = AesUtil.generateKey(256);
        byte[] plaintext = TEST_PLAINTEXT.getBytes();
        
        byte[] ciphertext = AesUtil.encrypt(plaintext, key);
        assertNotNull(ciphertext);
        assertTrue(ciphertext.length > plaintext.length); // 包含 IV 和 Tag
        
        byte[] decrypted = AesUtil.decrypt(ciphertext, key);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void testEncryptWith128BitKey() {
        // 测试 128 位密钥加密
        byte[] key = AesUtil.generateKey(128);
        String ciphertext = AesUtil.encrypt(TEST_PLAINTEXT, key);
        String decrypted = AesUtil.decrypt(ciphertext, key);
        assertEquals(TEST_PLAINTEXT, decrypted);
    }

    @Test
    void testEncryptWith192BitKey() {
        // 测试 192 位密钥加密
        byte[] key = AesUtil.generateKey(192);
        String ciphertext = AesUtil.encrypt(TEST_PLAINTEXT, key);
        String decrypted = AesUtil.decrypt(ciphertext, key);
        assertEquals(TEST_PLAINTEXT, decrypted);
    }

    @Test
    void testEncryptNullPlaintext() {
        // 测试 null 明文
        byte[] key = AesUtil.generateKey(256);
        assertThrows(CryptoException.class, () -> AesUtil.encrypt((String) null, key));
        assertThrows(CryptoException.class, () -> AesUtil.encrypt((byte[]) null, key));
    }

    @Test
    void testEncryptNullKey() {
        // 测试 null 密钥
        assertThrows(CryptoException.class, () -> AesUtil.encrypt(TEST_PLAINTEXT, null));
    }

    @Test
    void testEncryptInvalidKeySize() {
        // 测试无效的密钥大小
        byte[] invalidKey = new byte[15]; // 15 字节，无效
        assertThrows(CryptoException.class, () -> AesUtil.encrypt(TEST_PLAINTEXT, invalidKey));
    }

    @Test
    void testDecryptNullCiphertext() {
        // 测试 null 密文
        byte[] key = AesUtil.generateKey(256);
        assertThrows(CryptoException.class, () -> AesUtil.decrypt((String) null, key));
        assertThrows(CryptoException.class, () -> AesUtil.decrypt((byte[]) null, key));
    }

    @Test
    void testDecryptInvalidCiphertext() {
        // 测试无效的密文（长度不足）
        byte[] key = AesUtil.generateKey(256);
        byte[] invalidCiphertext = new byte[5]; // 长度不足 12 字节（IV 长度）
        assertThrows(CryptoException.class, () -> AesUtil.decrypt(invalidCiphertext, key));
    }

    @Test
    void testDecryptWithWrongKey() {
        // 测试使用错误的密钥解密
        byte[] key1 = AesUtil.generateKey(256);
        byte[] key2 = AesUtil.generateKey(256);
        
        String ciphertext = AesUtil.encrypt(TEST_PLAINTEXT, key1);
        assertThrows(CryptoException.class, () -> AesUtil.decrypt(ciphertext, key2));
    }

    @Test
    void testRandomIV() {
        // 测试每次加密使用不同的 IV
        byte[] key = AesUtil.generateKey(256);
        
        String ciphertext1 = AesUtil.encrypt(TEST_PLAINTEXT, key);
        String ciphertext2 = AesUtil.encrypt(TEST_PLAINTEXT, key);
        
        // 相同明文和密钥，不同密文（因为 IV 不同）
        assertNotEquals(ciphertext1, ciphertext2);
        
        // 但解密后应该相同
        String decrypted1 = AesUtil.decrypt(ciphertext1, key);
        String decrypted2 = AesUtil.decrypt(ciphertext2, key);
        assertEquals(decrypted1, decrypted2);
        assertEquals(TEST_PLAINTEXT, decrypted1);
    }

    @Test
    void testLongText() {
        // 测试较长文本的加密解密
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("The quick brown fox jumps over the lazy dog. ");
        }
        String longText = sb.toString();
        
        byte[] key = AesUtil.generateKey(256);
        String ciphertext = AesUtil.encrypt(longText, key);
        String decrypted = AesUtil.decrypt(ciphertext, key);
        
        assertEquals(longText, decrypted);
    }

    @Test
    void testEmptyString() {
        // 测试空字符串加密解密
        byte[] key = AesUtil.generateKey(256);
        String ciphertext = AesUtil.encrypt("", key);
        String decrypted = AesUtil.decrypt(ciphertext, key);
        assertEquals("", decrypted);
    }

    @Test
    void testChineseText() {
        // 测试中文文本加密解密
        String chineseText = "你好，世界！这是一个测试。";
        byte[] key = AesUtil.generateKey(256);
        
        String ciphertext = AesUtil.encrypt(chineseText, key);
        String decrypted = AesUtil.decrypt(ciphertext, key);
        
        assertEquals(chineseText, decrypted);
    }

    @Test
    void testSpecialCharacters() {
        // 测试特殊字符加密解密
        String specialText = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
        byte[] key = AesUtil.generateKey(256);
        
        String ciphertext = AesUtil.encrypt(specialText, key);
        String decrypted = AesUtil.decrypt(ciphertext, key);
        
        assertEquals(specialText, decrypted);
    }
}
