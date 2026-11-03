package top.wyhao.system.admin.app.service;

import org.springframework.web.multipart.MultipartFile;
import top.wyhao.system.admin.domain.model.File;
import top.wyhao.mybatis.query.PageResult;

import java.io.InputStream;
import java.util.List;

/**
 * 文件服务接口
 *
 * <p>负责：
 * <ul>
 *     <li>生成 storageKey</li>
 *     <li>调用 StorageManager 进行存储</li>
 *     <li>保存 FILE 元数据</li>
 *     <li>下载文件</li>
 *     <li>文件逻辑删除</li>
 * </ul>
 *
 * @author wyh
 * @since 2026/09/16
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file       文件
     * @param operatorId 操作人 ID
     * @return 文件实体
     */
    File upload(MultipartFile file, Long operatorId);

    /**
     * 上传文件
     *
     * @param fileName     文件名
     * @param contentType  内容类型
     * @param contentLength 内容长度
     * @param inputStream  文件输入流
     * @param operatorId   操作人 ID
     * @return 文件实体
     */
    File upload(String fileName, String contentType, long contentLength,
                InputStream inputStream, Long operatorId);

    /**
     * 根据文件 ID 获取文件信息
     *
     * @param fileId 文件 ID
     * @return 文件实体
     */
    File get(Long fileId);

    /**
     * 下载文件
     *
     * @param fileId 文件 ID
     * @return 文件输入流
     */
    InputStream download(Long fileId);

    /**
     * 删除文件（逻辑删除）
     *
     * @param fileId     文件 ID
     * @param operatorId 操作人 ID
     */
    void delete(Long fileId, Long operatorId);

    /**
     * 检查文件是否存在且可访问
     *
     * @param fileId 文件 ID
     * @return 是否存在且可访问
     */
    boolean exists(Long fileId);

    /**
     * 分页查询文件
     *
     * @param fileName  文件名（模糊，可空）
     * @param category  文件分类（可空，默认全部）
     * @param sortOrder 时间排序：asc / desc，默认 desc
     * @param page      页码（从 1 开始）
     * @param size      每页条数
     * @return 分页结果
     */
    PageResult<File> page(String fileName, String category, String sortOrder, long page, long size);

    /**
     * 批量逻辑删除
     *
     * @param fileIds    文件 ID 列表
     * @param operator 操作人 ID
     */
    void delete(List<Long> fileIds, Long operator);

    /**
     * 计算文件的 SHA-256 哈希值
     *
     * @param inputStream 文件输入流
     * @return SHA-256 哈希值（小写十六进制字符串）
     */
    String calculateSha256(InputStream inputStream);
}
