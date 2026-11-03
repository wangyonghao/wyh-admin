package top.wyhao.web.util;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import top.wyhao.cmn.core.exception.SystemException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * Http工具类
 */
public class HttpUtil {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * 允许浏览器内联预览的 MIME 类型；其他类型（如 text/html、image/svg+xml）一律按附件下载，防止存储型 XSS
     */
    private static final Set<String> INLINE_SAFE_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "image/webp", "image/bmp",
            "application/pdf", "text/plain");

    /**
     * 下载文件
     *
     * @param inputStream  输入流
     * @param filename     文件名
     * @param response     响应
     */
    public static void writeAttachmentToResponse(InputStream inputStream, String filename, HttpServletResponse response) {
        try (inputStream) {
            response.setContentType(DEFAULT_CONTENT_TYPE);
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encode(filename));
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new SystemException("下载文件失败", e);
        }
    }

    /**
     * 将文件流输出到 HTTP 响应，供浏览器直接预览
     *
     * <p>仅安全类型使用 inline，其余类型退化为附件下载
     *
     * @param inputStream  文件输入流
     * @param filename     原始文件名（用于浏览器显示）
     * @param contentType  服务端确定的 MIME 类型，不要传入客户端提交的值
     * @param response     HTTP 响应对象
     */
    public static void preview(InputStream inputStream, String filename, String contentType, HttpServletResponse response) {
        String mimeType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        boolean inline = INLINE_SAFE_CONTENT_TYPES.contains(mimeType);
        try (inputStream) {
            response.setContentType(inline ? mimeType : DEFAULT_CONTENT_TYPE);
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    (inline ? "inline" : "attachment") + "; filename*=UTF-8''" + encode(filename));
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new SystemException("预览文件失败", e);
        }
    }

    /**
     * 便捷重载：根据文件名后缀推断 MIME 类型
     */
    public static void preview(InputStream inputStream, String filename, HttpServletResponse response) {
        String mimeType = java.net.URLConnection.guessContentTypeFromName(filename);
        preview(inputStream, filename, mimeType, response);
    }

    private static String encode(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8);
    }
}
