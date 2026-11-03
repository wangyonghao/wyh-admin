package top.wyhao.crypto;

import org.junit.jupiter.api.Test;
import top.wyhao.crypto.exception.CryptoException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DigestUtil 单元测试
 *
 * @author wyh
 * @since 2026/09/16
 */
class DigestUtilTest {

    private static final String TEST_TEXT = "Hello, World!";

    @Test
    void testMd5String() {
        // 测试 MD5 摘要（字符串）
        String md5 = DigestUtil.md5(TEST_TEXT);
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5);
        assertEquals(32, md5.length()); // MD5 固定 32 个字符
    }

    @Test
    void testMd5Bytes() {
        // 测试 MD5 摘要（字节数组）
        String md5 = DigestUtil.md5(TEST_TEXT.getBytes());
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5);
    }

    @Test
    void testMd5InputStream() {
        // 测试 MD5 摘要（输入流）
        InputStream input = new ByteArrayInputStream(TEST_TEXT.getBytes());
        String md5 = DigestUtil.md5(input);
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5);
    }

    @Test
    void testSha1String() {
        // 测试 SHA-1 摘要（字符串）
        String sha1 = DigestUtil.sha1(TEST_TEXT);
        assertEquals("0a0a9f2a6772942557ab5355d76af442f8f65e01", sha1);
        assertEquals(40, sha1.length()); // SHA-1 固定 40 个字符
    }

    @Test
    void testSha1Bytes() {
        // 测试 SHA-1 摘要（字节数组）
        String sha1 = DigestUtil.sha1(TEST_TEXT.getBytes());
        assertEquals("0a0a9f2a6772942557ab5355d76af442f8f65e01", sha1);
    }

    @Test
    void testSha1InputStream() {
        // 测试 SHA-1 摘要（输入流）
        InputStream input = new ByteArrayInputStream(TEST_TEXT.getBytes());
        String sha1 = DigestUtil.sha1(input);
        assertEquals("0a0a9f2a6772942557ab5355d76af442f8f65e01", sha1);
    }

    @Test
    void testSha256String() {
        // 测试 SHA-256 摘要（字符串）
        String sha256 = DigestUtil.sha256(TEST_TEXT);
        assertEquals("dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f", sha256);
        assertEquals(64, sha256.length()); // SHA-256 固定 64 个字符
    }

    @Test
    void testSha256Bytes() {
        // 测试 SHA-256 摘要（字节数组）
        String sha256 = DigestUtil.sha256(TEST_TEXT.getBytes());
        assertEquals("dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f", sha256);
    }

    @Test
    void testSha256InputStream() {
        // 测试 SHA-256 摘要（输入流）
        InputStream input = new ByteArrayInputStream(TEST_TEXT.getBytes());
        String sha256 = DigestUtil.sha256(input);
        assertEquals("dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f", sha256);
    }

    @Test
    void testSha512String() {
        // 测试 SHA-512 摘要（字符串）
        String sha512 = DigestUtil.sha512(TEST_TEXT);
        assertEquals(128, sha512.length()); // SHA-512 固定 128 个字符
        assertTrue(sha512.matches("[0-9a-f]{128}")); // 验证格式
    }

    @Test
    void testSha512Bytes() {
        // 测试 SHA-512 摘要（字节数组）
        String sha512 = DigestUtil.sha512(TEST_TEXT.getBytes());
        assertEquals(128, sha512.length());
    }

    @Test
    void testSha512InputStream() {
        // 测试 SHA-512 摘要（输入流）
        InputStream input = new ByteArrayInputStream(TEST_TEXT.getBytes());
        String sha512 = DigestUtil.sha512(input);
        assertEquals(128, sha512.length());
    }

    @Test
    void testDigestNullInputs() {
        // 测试 null 输入
        assertThrows(CryptoException.class, () -> DigestUtil.md5((String) null));
        assertThrows(CryptoException.class, () -> DigestUtil.md5((byte[]) null));
        assertThrows(CryptoException.class, () -> DigestUtil.md5((InputStream) null));
        assertThrows(CryptoException.class, () -> DigestUtil.sha256((String) null));
    }

    @Test
    void testDigestEmptyString() {
        // 测试空字符串
        String md5 = DigestUtil.md5("");
        assertNotNull(md5);
        assertEquals(32, md5.length());
    }

    @Test
    void testDigestGeneric() {
        // 测试通用摘要方法
        String md5 = DigestUtil.digest(TEST_TEXT, "MD5");
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5);

        String sha256 = DigestUtil.digest(TEST_TEXT, "SHA-256");
        assertEquals("dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f", sha256);
    }

    @Test
    void testDigestUnsupportedAlgorithm() {
        // 测试不支持的算法
        assertThrows(CryptoException.class, () -> DigestUtil.digest(TEST_TEXT, "UNSUPPORTED"));
    }

    @Test
    void testDigestEmptyAlgorithm() {
        // 测试空算法名
        assertThrows(CryptoException.class, () -> DigestUtil.digest(TEST_TEXT, ""));
        assertThrows(CryptoException.class, () -> DigestUtil.digest(TEST_TEXT, null));
    }

    @Test
    void testConsistency() {
        // 测试相同输入产生相同输出
        String digest1 = DigestUtil.sha256(TEST_TEXT);
        String digest2 = DigestUtil.sha256(TEST_TEXT);
        assertEquals(digest1, digest2);
    }

    @Test
    void testDifferentInputsDifferentOutputs() {
        // 测试不同输入产生不同输出
        String digest1 = DigestUtil.sha256("Hello");
        String digest2 = DigestUtil.sha256("World");
        assertNotEquals(digest1, digest2);
    }
}
