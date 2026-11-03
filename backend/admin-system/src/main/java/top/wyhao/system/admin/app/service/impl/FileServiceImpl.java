package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import top.wyhao.mybatis.query.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.system.admin.app.service.FileService;
import top.wyhao.system.admin.domain.enums.FileCategory;
import top.wyhao.system.admin.domain.enums.FileStatus;
import top.wyhao.system.admin.domain.exception.FileException;
import top.wyhao.system.admin.domain.gateway.FileRepository;
import top.wyhao.system.admin.domain.model.File;
import top.wyhao.system.admin.infrastructure.config.FileCoreProperties;
import top.wyhao.system.admin.infrastructure.storage.StorageManager;
import top.wyhao.storage.api.StorageException;
import top.wyhao.storage.api.StorageObject;
import top.wyhao.storage.api.StorageType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 文件服务实现
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * 扩展名 -> Hutool FileTypeUtil 识别出的类型
     */
    private static final Map<String, String> MAGIC_CHECKED_TYPES = Map.of(
            "jpg", "jpg",
            "jpeg", "jpg",
            "png", "png",
            "gif", "gif",
            "bmp", "bmp",
            "webp", "webp",
            "pdf", "pdf");

    private final FileRepository fileRepository;
    private final StorageManager storageManager;
    private final FileCoreProperties properties;

    @Override
    public File upload(MultipartFile file, Long operatorId) {
        if (file == null || file.isEmpty()) {
            throw new StorageException(StorageType.LOCAL, "上传文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long contentLength = file.getSize();

        try {
            return upload(fileName, contentType, contentLength, file.getInputStream(), operatorId);
        } catch (IOException e) {
            throw new StorageException(StorageType.LOCAL, "读取上传文件失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public File upload(String fileName, String clientContentType, long contentLength,
                       InputStream inputStream, Long operatorId) {
        String extension = checkExtensionAllowed(fileName);

        // 生成存储键
        String storageKey = generateStorageKey(fileName);

        // 计算文件哈希值
        // 先读取流到内存，以便多次使用（计算哈希 + 上传）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IoUtil.copy(inputStream, baos);
        } catch (Exception e) {
            throw new StorageException("复制文件流失败", e);
        }
        byte[] data = baos.toByteArray();
        checkContentMatchesExtension(data, extension);
        String contentType = resolveContentType(fileName);
        ByteArrayInputStream hashStream = new ByteArrayInputStream(data);
        String sha256 = calculateSha256(hashStream);

        // 上传到存储
        ByteArrayInputStream uploadStream = new ByteArrayInputStream(data);
        StorageType storageType = storageManager.getDefault().type();
        try {
            storageManager.getDefault().put(storageKey, uploadStream, contentLength, contentType);
        } catch (Exception e) {
            log.error("上传文件到存储失败: storageKey={}", storageKey, e);
            throw new StorageException(storageType, "上传文件到存储失败", e);
        }

        // 保存文件元数据
        File file = File.create(fileName, contentType, contentLength, sha256, storageType, storageKey);
        file.setCreateUser(operatorId);
        file.setUpdateUser(operatorId);

        try {
            fileRepository.insert(file);
        } catch (Exception e) {
            // 数据库保存失败，尝试删除已上传的文件
            log.error("保存文件元数据失败，尝试删除已上传的文件: storageKey={}", storageKey, e);
            try {
                storageManager.getDefault().delete(storageKey);
            } catch (Exception deleteEx) {
                log.error("删除已上传的文件失败: storageKey={}", storageKey, deleteEx);
            }
            throw new StorageException(storageType, "保存文件元数据失败", e);
        }

        log.info("文件上传成功: fileId={}, fileName={}, storageKey={}, sha256={}",
                file.getId(), fileName, storageKey, sha256);

        return file;
    }

    @Override
    public File get(Long fileId) {
        File file = fileRepository.findById(fileId);
        if (file == null) {
            throw FileException.notFound(fileId);
        }
        return file;
    }

    @Override
    public InputStream download(Long fileId) {
        File file = get(fileId);

        if (!file.isAccessible()) {
            throw FileException.notFound(fileId);
        }

        StorageObject storageObject = storageManager.get(file.getStorageType()).get(file.getStorageKey());
        if (storageObject == null) {
            throw FileException.notFound(file.getStorageKey());
        }

        return storageObject.getInputStream();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long fileId, Long operatorId) {
        File file = get(fileId);

        if (FileStatus.ACTIVE.equals(file.getStatus())) {
            // 逻辑删除
            file.markDeleted();
            file.setUpdateUser(operatorId);
            fileRepository.updateById(file);

            log.info("文件逻辑删除成功: fileId={}, operatorId={}", fileId, operatorId);
        } else {
            log.warn("文件状态异常，无法删除: fileId={}, status={}", fileId, file.getStatus());
        }
    }

    @Override
    public boolean exists(Long fileId) {
        if (fileId == null) {
            return false;
        }
        File file = fileRepository.findById(fileId);
        return file != null && file.isAccessible();
    }

    @Override
    public PageResult<File> page(String fileName, String category, String sortOrder, long page, long size) {
        FileCategory fileCategory = FileCategory.of(category);
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        return fileRepository.page(fileName, fileCategory, asc, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> fileIds, Long operator) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        for (Long fileId : fileIds) {
            delete(fileId, operator);
        }
    }

    @Override
    public String calculateSha256(InputStream inputStream) {
        try {
            return DigestUtil.sha256Hex(inputStream);
        } catch (Exception e) {
            throw FileException.hashFailed(e);
        } finally {
            IoUtil.close(inputStream);
        }
    }

    /**
     * 校验扩展名是否在上传白名单内
     *
     * @param fileName 原始文件名
     * @return 小写扩展名
     */
    private String checkExtensionAllowed(String fileName) {
        String extension = StrUtil.nullToEmpty(FileUtil.extName(fileName)).toLowerCase(Locale.ROOT);
        if (extension.isEmpty() || !properties.getUpload().getAllowedExtensions().contains(extension)) {
            throw FileException.extensionNotAllowed(extension);
        }
        return extension;
    }

    /**
     * 对有固定文件头的类型，校验文件内容与扩展名一致，防止伪装扩展名
     *
     * @param data      文件内容
     * @param extension 小写扩展名
     */
    private void checkContentMatchesExtension(byte[] data, String extension) {
        String expectedType = MAGIC_CHECKED_TYPES.get(extension);
        if (expectedType == null) {
            return;
        }
        String actualType = FileTypeUtil.getType(new ByteArrayInputStream(data));
        if (!expectedType.equals(actualType)) {
            throw FileException.contentMismatch(extension);
        }
    }

    /**
     * 根据文件名推断 MIME 类型，不信任客户端提交的 Content-Type
     *
     * @param fileName 原始文件名
     * @return MIME 类型
     */
    private String resolveContentType(String fileName) {
        String mimeType = FileUtil.getMimeType(fileName);
        return StrUtil.isBlank(mimeType) ? DEFAULT_CONTENT_TYPE : mimeType;
    }

    /**
     * 生成存储键
     *
     * <p>格式：[{key-prefix}/]{yyyy}/{MM}/{dd}/{uuid}[.ext]
     * <p>前缀由 {@code file.storage.key-prefix} 配置，可空。
     *
     * @param fileName 原始文件名
     * @return 存储键
     */
    private String generateStorageKey(String fileName) {
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = IdUtil.fastSimpleUUID();
        String extension = FileUtil.extName(fileName);

        String objectName = (extension != null && !extension.isEmpty())
                ? datePath + "/" + uuid + "." + extension
                : datePath + "/" + uuid;

        String prefix = properties.getStorage().getKeyPrefix();
        if (prefix == null || prefix.isBlank()) {
            return objectName;
        }
        String normalized = prefix.trim().replaceAll("^/+|/+$", "");
        return normalized.isEmpty() ? objectName : normalized + "/" + objectName;
    }
}
