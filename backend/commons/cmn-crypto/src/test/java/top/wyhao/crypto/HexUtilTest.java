package top.wyhao.crypto;

import org.junit.jupiter.api.Test;
import top.wyhao.crypto.exception.CryptoException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HexUtil 单元测试
 *
 * @author wyh
 * @since 2026/09/16
 */
class HexUtilTest {

    @Test
    void testEncode() {
        // 测试普通字节数组编码
        byte[] data = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};
        String hex = HexUtil.encode(data);
        assertEquals("0123456789abcdef", hex);

        // 测试空字节数组
        String emptyHex = HexUtil.encode(new byte[0]);
        assertEquals("", emptyHex);

        // 测试单个字节
        String singleByte = HexUtil.encode(new byte[]{0x0f});
        assertEquals("0f", singleByte);
    }

    @Test
    void testEncodeNull() {
        // 测试 null 输入
        assertThrows(CryptoException.class, () -> HexUtil.encode(null));
    }

    @Test
    void testDecode() {
        // 测试普通十六进制解码
        String hex = "0123456789abcdef";
        byte[] data = HexUtil.decode(hex);
        assertArrayEquals(new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, data);

        // 测试大写十六进制
        byte[] upperData = HexUtil.decode("0123456789ABCDEF");
        assertArrayEquals(new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, upperData);

        // 测试空字符串
        byte[] emptyData = HexUtil.decode("");
        assertArrayEquals(new byte[0], emptyData);
    }

    @Test
    void testDecodeNull() {
        // 测试 null 输入
        assertThrows(CryptoException.class, () -> HexUtil.decode(null));
    }

    @Test
    void testDecodeInvalidLength() {
        // 测试奇数长度的十六进制字符串
        assertThrows(CryptoException.class, () -> HexUtil.decode("abc"));
    }

    @Test
    void testEncodeDecodeRoundTrip() {
        // 测试编码解码往返
        byte[] original = "Hello, World!".getBytes();
        String hex = HexUtil.encode(original);
        byte[] decoded = HexUtil.decode(hex);
        assertArrayEquals(original, decoded);
    }
}
