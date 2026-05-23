package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.admin.cmn.oss.FileStorage;
import top.wyhao.admin.cmn.oss.FileStorageFactory;
import top.wyhao.admin.system.entity.SysFile;
import top.wyhao.admin.system.mapper.SysFileMapper;
import top.wyhao.admin.system.model.enums.FileType;
import top.wyhao.admin.system.model.query.FileQuery;
import top.wyhao.admin.system.model.vo.file.FileResult;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.cmn.db.util.QueryWrapperUtil;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 文件业务实现
 *

 * @since 2023/12/23 10:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final SysFileMapper fileMapper;

    private final FileStorageFactory fileStorageFactory;


    public void delete(@NotNull List<Long> fileId) {
        if (fileId.size() > 100) {
            throw new BadRequestException("FILE_TOO_MANY", "同时最多只能删除 100 个文件");
        }
        List<SysFile> fileList = fileMapper.lambdaQuery().in(SysFile::getId, fileId).list();
        if (CollUtil.isEmpty(fileList)) {
            return;
        }
        fileList.forEach(sysFile -> fileStorageFactory.getStorage().delete(sysFile.getOssPath()));
    }

    @Override
    public void delete(@NotNull Long bizId, @NotNull String bizType) {
        fileMapper.delete(fileMapper.lambdaQuery().eq(SysFile::getBizId, bizId).eq(SysFile::getBizType, bizType));
    }

    @Override
    public InputStream getFileInputStream(Long fileId) {
        SysFile sysFile = this.detail(fileId);
        return fileStorageFactory.getStorage().getFileInputStream(sysFile.getOssPath());
    }

    public void delete(String fileUrl, String platform) {
        String urlPrefix = "http://127.0.0.1:8000/file/";
        if (!StrUtil.startWith(fileUrl, urlPrefix)) {
            throw new BadRequestException("FILE_URL_ERROR", "文件地址错误");
        }
        String relativePath = StrUtil.removePrefix(fileUrl, urlPrefix);

        // 删除数据库中的文件信息
        SysFile dbFile = fileMapper.lambdaQuery().eq(SysFile::getOssPath, relativePath).eq(SysFile::getOssPlatform, platform).one();
        if (dbFile == null) {
            throw new BadRequestException("FILE_NOT_FOUND", "文件不存在");
        }
        fileMapper.deleteById(dbFile.getId());

    }

    @Override
    public List<SysFile> list(FileQuery query) {
        return fileMapper.selectList(QueryWrapperUtil.build(query));
    }

    /**
     * 上传文件并返回文件信息
     *
     * @param file 文件
     * @param path 目标路径
     * @return 文件信息
     */
    public SysFile upload(MultipartFile file, String path) {
        // 验证文件大小限制

        // 验证文件类型限制
        String ext = FileNameUtil.extName(file.getOriginalFilename());
        List<String> allExtensions = FileType.getAllExtensions();
        if (!allExtensions.contains(ext)) {
            throw new BusinessException("FILE_UNSUPPORTED_TYPE", StrUtil.format("不支持的文件类型，仅支持 {} 格式的文件", String.join(",", allExtensions)));
        }
        // 获取存储实例
        FileStorage storage = fileStorageFactory.getStorage();

        String ossFilename = generateFileName(ext);
        String ossPath = StringUtils.hasText(path) ? path : generatePath();

        try {
            // 上传文件
            String url = storage.upload(file.getInputStream(), ossPath, ossFilename);

            // 保存文件记录
            SysFile sysFile = new SysFile();
            sysFile.setFileName(file.getOriginalFilename());
            sysFile.setOssFileName(ossFilename);
            sysFile.setOssPath(ossPath + "/" + ossFilename);
            sysFile.setOssUrl(url);
            sysFile.setFileSize(file.getSize());
            sysFile.setFileType(file.getContentType());
            sysFile.setFileExtension(ext);
            sysFile.setOssPlatform(storage.getStorageType());
            sysFile.setStatus("AVAILABLE");
            fileMapper.insert(sysFile);

            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), url);
            return sysFile;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public PageResult<FileResult> page(FileQuery query, PageQuery pageQuery) {
        IPage<SysFile> page = fileMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), QueryWrapperUtil.build(query));
        return PageResult.build(page, FileResult.class);
    }

    @Override
    public SysFile detail(Long id) {
        SysFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("FILE_NOT_FOUND", "文件不存在");
        }
        return file;
    }


    /**
     * 生成文件名
     */
    private String generateFileName(String extName) {
        return UUID.randomUUID().toString().replace("-", "") +"."+ extName;
    }

    /**
     * 生成存储路径（按日期）
     */
    private String generatePath() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
}