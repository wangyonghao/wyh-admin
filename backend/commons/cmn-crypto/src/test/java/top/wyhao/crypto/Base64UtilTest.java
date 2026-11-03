package top.wyhao.crypto;

import org.junit.jupiter.api.Test;
import top.wyhao.crypto.exception.CryptoException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base64Util 单元测试
 *
 * @author wyh
 * @since 2026/09/16
 */
class Base64UtilTest {

    @Test
    void testEncodeBytes() {
        // 测试字节数组编码
        byte[] data = "Hello, World!".getBytes();
        String base64 = Base64Util.encode(data);
        assertEquals("SGVsbG8sIFdvcmxkIQ==", base64);

        // 测试空字节数组
        String emptyBase64 = Base64Util.encode(new byte[0]);
        assertEquals("", emptyBase64);
    }

    @Test
    void testEncodeString() {
        // 测试字符串编码
        String text = "Hello, World!";
        String base64 = Base64Util.encode(text);
        assertEquals("SGVsbG8sIFdvcmxkIQ==", base64);

        // 测试中文字符串
        String chinese = Base64Util.encode("你好，世界！");
        assertNotNull(chinese);
        assertFalse(chinese.isEmpty());
    }

    @Test
    void testEncodeNull() {
        // 测试 null 输入
        assertThrows(CryptoException.class, () -> Base64Util.encode((byte[]) null));
        assertThrows(CryptoException.class, () -> Base64Util.encode((String) null));
    }

    @Test
    void testDecode() {
        // 测试 Base64 解码
        String base64 = "SGVsbG8sIFdvcmxkIQ==";
        byte[] data = Base64Util.decode(base64);
        assertArrayEquals("Hello, World!".getBytes(), data);
    }

    @Test
    void testDecodeToString() {
        // 测试解码为字符串
        String base64 = "SGVsbG8sIFdvcmxkIQ==";
        String text = Base64Util.decodeToString(base64);
        assertEquals("Hello, World!", text);
    }

    @Test
    void testDecodeNull() {
        // 测试 null 输入
        assertThrows(CryptoException.class, () -> Base64Util.decode(null));
        assertThrows(CryptoException.class, () -> Base64Util.decodeToString(null));
    }

    @Test
    void testDecodeInvalid() {
        // 测试非法 Base64 字符串
        assertThrows(CryptoException.class, () -> Base64Util.decode("Not-A-Valid-Base64!!!"));
    }

    @Test
    void testUrlSafeEncode() {
        // 测试 URL Safe 编码
        byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        String urlSafe = Base64Util.encodeUrlSafe(data);
        // URL Safe 不包含填充
        assertFalse(urlSafe.contains("="));
        assertFalse(urlSafe.contains("+"));
        assertFalse(urlSafe.contains("/"));
    }

    @Test
    void testUrlSafeEncodeString() {
        // 测试 URL Safe 字符串编码
        String text = "Hello, World!";
        String urlSafe = Base64Util.encodeUrlSafe(text);
        assertNotNull(urlSafe);
    }

    @Test
    void testUrlSafeDecode() {
        // 测试 URL Safe 解码
        String urlSafe = Base64Util.encodeUrlSafe("Hello, World!".getBytes());
        byte[] decoded = Base64Util.decodeUrlSafe(urlSafe);
        assertArrayEquals("Hello, World!".getBytes(), decoded);
    }

    @Test
    void testUrlSafeDecodeToString() {
        // 测试 URL Safe 解码为字符串
        String urlSafe = Base64Util.encodeUrlSafe("Hello, World!");
        String text = Base64Util.decodeUrlSafeToString(urlSafe);
        assertEquals("Hello, World!", text);
    }

    @Test
    void testEncodeDecodeRoundTrip() {
        // 测试编码解码往返
        String original = "The quick brown fox jumps over the lazy dog";
        String base64 = Base64Util.encode(original);
        String decoded = Base64Util.decodeToString(base64);
        assertEquals(original, decoded);
    }

    @Test
    void testUrlSafeRoundTrip() {
        // 测试 URL Safe 编码解码往返
        String original = "The quick brown fox jumps over the lazy dog";
        String urlSafe = Base64Util.encodeUrlSafe(original);
        String decoded = Base64Util.decodeUrlSafeToString(urlSafe);
        assertEquals(original, decoded);
    }
}
