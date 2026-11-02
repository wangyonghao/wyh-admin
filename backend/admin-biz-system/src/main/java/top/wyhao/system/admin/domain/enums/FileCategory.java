package top.wyhao.system.admin.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 文件分类（按扩展名/ MIME 筛选，非业务领域）
 *
 * @author wyh
 * @since 2026/09/18
 */
@Getter
@RequiredArgsConstructor
public enum FileCategory {

    ALL("全部", Collections.emptyList()),

    ARCHIVE("压缩包", List.of("zip", "rar", "7z", "tar", "gz", "tgz", "bz2")),

    DOCUMENT("文档", List.of(
            "txt", "md", "doc", "docx", "rtf", "odt",
            "xls", "xlsx", "csv", "ods",
            "ppt", "pptx", "odp",
            "pdf"
    )),

    DOCUMENT_TEXT("文本", List.of("txt", "md", "doc", "docx", "rtf", "odt")),

    DOCUMENT_SPREADSHEET("表格", List.of("xls", "xlsx", "csv", "ods")),

    DOCUMENT_PRESENTATION("幻灯片", List.of("ppt", "pptx", "odp")),

    DOCUMENT_PDF("PDF", List.of("pdf")),

    IMAGE("图片", List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico")),

    MEDIA("多媒体", List.of(
            "mp3", "wav", "flac", "aac", "ogg", "m4a",
            "mp4", "avi", "mov", "mkv", "wmv", "flv", "webm"
    ));

    private final String label;
    private final List<String> extensions;

    public static FileCategory of(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(ALL);
    }

    /**
     * 是否匹配文件名扩展名
     */
    public boolean matchesFileName(String fileName) {
        if (this == ALL) {
            return true;
        }
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return extensions.contains(ext);
    }
}
