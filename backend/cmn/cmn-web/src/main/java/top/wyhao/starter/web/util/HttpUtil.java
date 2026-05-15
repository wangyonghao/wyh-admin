package top.wyhao.starter.web.util;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import top.wyhao.starter.core.exception.SystemException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Http工具类
 */
public class HttpUtil {

    /**
     * 下载文件
     *
     * @param inputStream  输入流
     * @param filename     文件名
     * @param response     响应
     */
    public static void writeAttachmentToResponse(InputStream inputStream, String filename, HttpServletResponse response) {
        try {
            // 设置响应头
            String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8); // 文件名编码，防止中文乱码
            response.setContentType("application/octet-stream");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);
            // 拷贝流
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new SystemException("下载文件失败", e);
        }
    }

    /**
     * 将文件流输出到 HTTP 响应，供浏览器直接预览
     *
     * @param inputStream  文件输入流
     * @param filename     原始文件名（用于浏览器显示）
     * @param contentType  MIME 类型，如 image/jpeg、application/pdf、text/plain 等
     * @param response     HTTP 响应对象
     */
    public static void preview(InputStream inputStream, String filename, String contentType, HttpServletResponse response) {
        try {
            // 1. 设置 MIME 类型（关键：决定浏览器如何渲染内容）
            response.setContentType(contentType != null ? contentType : "application/octet-stream");

            // 2. 使用 inline 模式，浏览器将尝试直接在页面中打开/预览
            String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedName);

            // 3. 流拷贝
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new SystemException("预览文件失败", e);
        }
    }

    /**
     * 便捷重载：自动根据文件名后缀推断 MIME 类型
     */
    public static void preview(InputStream inputStream, String filename, HttpServletResponse response) {
        String mimeType = java.net.URLConnection.guessContentTypeFromName(filename);
        preview(inputStream, filename, mimeType, response);
    }
}
