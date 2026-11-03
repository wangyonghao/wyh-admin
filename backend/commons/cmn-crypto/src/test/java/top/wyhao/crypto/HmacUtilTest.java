package top.wyhao.crypto;

import org.junit.jupiter.api.Test;
import top.wyhao.crypto.exception.CryptoException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HmacUtil 单元测试
 *
 * @author wyh
 * @since 2026/09/16
 */
class HmacUtilTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final String TEST_SECRET = "MySecretKey";

    @Test
    void testHmacSha256String() {
        // 测试 HmacSHA256（字符串）
        String hmac = HmacUtil.hmacSha256(TEST_DATA, TEST_SECRET);
        assertNotNull(hmac);
        assertEquals(64, hmac.length()); // SHA-256 HMAC 固定 64 个字符
        assertTrue(hmac.matches("[0-9a-f]{64}")); // 验证格式
    }

    @Test
    void testHmacSha256Bytes() {
        // 测试 HmacSHA256（字节数组）
        String hmac = HmacUtil.hmacSha256(
                TEST_DATA.getBytes(),
                TEST_SECRET.getBytes()
        );
        assertNotNull(hmac);
        assertEquals(64, hmac.length());
    }

    @Test
    void testHmacSha512String() {
        // 测试 HmacSHA512（字符串）
        String hmac = HmacUtil.hmacSha512(TEST_DATA, TEST_SECRET);
        assertNotNull(hmac);
        assertEquals(128, hmac.length()); // SHA-512 HMAC 固定 128 个字符
        assertTrue(hmac.matches("[0-9a-f]{128}"));
    }

    @Test
    void testHmacSha512Bytes() {
        // 测试 HmacSHA512（字节数组）
        String hmac = HmacUtil.hmacSha512(
                TEST_DATA.getBytes(),
                TEST_SECRET.getBytes()
        );
        assertNotNull(hmac);
        assertEquals(128, hmac.length());
    }

    @Test
    void testHmacNullInputs() {
        // 测试 null 输入
        assertThrows(CryptoException.class, () -> HmacUtil.hmacSha256(null, TEST_SECRET));
        assertThrows(CryptoException.class, () -> HmacUtil.hmacSha256(TEST_DATA, null));
        assertThrows(CryptoException.class, () -> HmacUtil.hmacSha256((byte[]) null, TEST_SECRET.getBytes()));
        assertThrows(CryptoException.class, () -> HmacUtil.hmacSha256(TEST_DATA.getBytes(), null));
    }

    @Test
    void testHmacEmptySecret() {
        // 测试空密钥
        assertThrows(CryptoException.class, () -> HmacUtil.hmacSha256(TEST_DATA.getBytes(), new byte[0]));
    }

    @Test
    void testHmacGeneric() {
        // 测试通用 HMAC 方法
        String hmac256 = HmacUtil.hmac(TEST_DATA, TEST_SECRET, "HmacSHA256");
        assertEquals(64, hmac256.length());

        String hmac512 = HmacUtil.hmac(TEST_DATA, TEST_SECRET, "HmacSHA512");
        assertEquals(128, hmac512.length());
    }

    @Test
    void testHmacUnsupportedAlgorithm() {
        // 测试不支持的算法
        assertThrows(CryptoException.class, () ->
                HmacUtil.hmac(TEST_DATA, TEST_SECRET, "UnsupportedAlgorithm"));
    }

    @Test
    void testHmacEmptyAlgorithm() {
        // 测试空算法名
        assertThrows(CryptoException.class, () ->
                HmacUtil.hmac(TEST_DATA, TEST_SECRET, ""));
        assertThrows(CryptoException.class, () ->
                HmacUtil.hmac(TEST_DATA, TEST_SECRET, null));
    }

    @Test
    void testConsistency() {
        // 测试相同输入产生相同输出
        String hmac1 = HmacUtil.hmacSha256(TEST_DATA, TEST_SECRET);
        String hmac2 = HmacUtil.hmacSha256(TEST_DATA, TEST_SECRET);
        assertEquals(hmac1, hmac2);
    }

    @Test
    void testDifferentSecretsDifferentOutputs() {
        // 测试不同密钥产生不同输出
        String hmac1 = HmacUtil.hmacSha256(TEST_DATA, "secret1");
        String hmac2 = HmacUtil.hmacSha256(TEST_DATA, "secret2");
        assertNotEquals(hmac1, hmac2);
    }

    @Test
    void testDifferentDataDifferentOutputs() {
        // 测试不同数据产生不同输出
        String hmac1 = HmacUtil.hmacSha256("data1", TEST_SECRET);
        String hmac2 = HmacUtil.hmacSha256("data2", TEST_SECRET);
        assertNotEquals(hmac1, hmac2);
    }

    @Test
    void testVerify() {
        // 测试 HMAC 验证
        String signature = HmacUtil.hmacSha256(TEST_DATA, TEST_SECRET);
        
        // 验证正确的签名
        assertTrue(HmacUtil.verify(TEST_DATA, TEST_SECRET, signature, "HmacSHA256"));
        
        // 验证错误的签名
        assertFalse(HmacUtil.verify(TEST_DATA, TEST_SECRET, "wrongsignature", "HmacSHA256"));
        
        // 验证空签名
        assertFalse(HmacUtil.verify(TEST_DATA, TEST_SECRET, null, "HmacSHA256"));
        assertFalse(HmacUtil.verify(TEST_DATA, TEST_SECRET, "", "HmacSHA256"));
    }

    @Test
    void testVerifyWithModifiedData() {
        // 测试修改数据后验证失败
        String signature = HmacUtil.hmacSha256(TEST_DATA, TEST_SECRET);
        assertFalse(HmacUtil.verify("Modified Data", TEST_SECRET, signature, "HmacSHA256"));
    }

    @Test
    void testVerifyWithModifiedSecret() {
        // 测试修改密钥后验证失败
        String signature = HmacUtil.hmacSha256(TEST_DATA, TEST_SECRET);
        assertFalse(HmacUtil.verify(TEST_DATA, "WrongSecret", signature, "HmacSHA256"));
    }
}
