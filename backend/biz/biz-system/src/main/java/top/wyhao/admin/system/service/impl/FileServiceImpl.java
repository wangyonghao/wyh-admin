/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.ProgressListener;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.admin.system.model.bo.FileReq;
import top.wyhao.admin.system.model.entity.FileDO;
import top.wyhao.admin.system.model.enums.FileType;
import top.wyhao.admin.system.model.query.FileQuery;
import top.wyhao.admin.system.model.vo.file.FileResp;
import top.wyhao.admin.system.model.vo.file.FileStatisticsResp;
import top.wyhao.admin.system.mapper.FileMapper;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.starter.cache.redisson.util.RedisLockUtils;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件业务实现
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageService fileStorageService;

    private final FileMapper fileMapper;

    public void delete(@NotNull List<Long> ids) {
        if(ids.size() > 100){
            throw new BadRequestException("FILE_TOO_MANY", "同时最多只能删除 100 个文件");
        }
        List<FileDO> fileList = fileMapper.lambdaQuery().in(FileDO::getId, ids).list();
        if (CollUtil.isEmpty(fileList)) {
            return;
        }
        fileList.stream().map(FileDO::toFileInfo).forEach(fileStorageService::delete);
    }

    @Override
    public void export(FileQuery query, SortQuery sortQuery, HttpServletResponse response) {

    }

    @Override
    public List<LabelValueResp> dict(FileQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public void update(FileReq req, Long id) {

    }

    @Override
    public FileInfo upload(MultipartFile file, String parentPath, String storageCode) throws IOException {
        return this.upload(file, parentPath, storageCode, FileNameUtil.extName(file.getOriginalFilename()));
    }

    @Override
    public FileInfo upload(File file, String parentPath, String storageCode) throws IOException {
        return this.upload(file, parentPath, storageCode, FileNameUtil.extName(file.getName()));
    }

    @Override
    public Long createDir(FileReq req) {
        String parentPath = req.getParentPath();
        FileDO file = fileMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getName, req.getOriginalName())
            .eq(FileDO::getType, FileType.DIR)
            .one();
        BizAssert.throwIfNotNull(file, "文件夹已存在");
        String platform = fileStorageService.getProperties().getDefaultPlatform();
        // 存储引擎需要一致
        if (!StringConstants.SLASH.equals(parentPath)) {
            FileDO parentFile = fileMapper.lambdaQuery()
                .eq(FileDO::getPath, parentPath)
                .eq(FileDO::getType, FileType.DIR)
                .one();
            BizAssert.isNull(parentFile, "父级文件夹不存在");
            BizAssert.throwIfNotEqual(parentFile.getStorageId(), platform, "文件夹和父级文件夹存储引擎不一致");
        }
        // 创建文件夹
        FileDO dirFile = new FileDO();
        String originalName = req.getOriginalName();
        dirFile.setName(originalName);
        dirFile.setOriginalName(originalName);
        dirFile.setParentPath(parentPath);
        dirFile.setType(FileType.DIR);
        dirFile.setStorageId(platform);
        fileMapper.insert(dirFile);
        return dirFile.getId();
    }

    @Override
    public FileStatisticsResp statistics() {
        FileStatisticsResp resp = new FileStatisticsResp();
        List<FileStatisticsResp> statisticsList = fileMapper.statistics();
        if (CollUtil.isEmpty(statisticsList)) {
            return resp;
        }
        resp.setData(statisticsList);
        resp.setSize(statisticsList.stream().mapToLong(FileStatisticsResp::getSize).sum());
        resp.setNumber(statisticsList.stream().mapToLong(FileStatisticsResp::getNumber).sum());
        return resp;
    }

    @Override
    public FileResp check(String fileHash) {
        FileDO file = this.fileMapper.lambdaQuery().eq(FileDO::getSha256, fileHash).one();
        if (file != null) {
            return get(file.getId());
        }
        return null;
    }

    @Override
    public Long calcDirSize(Long id) {
        FileDO dirFile = fileMapper.selectById(id);
        ValidationUtils.throwIfNotEqual(dirFile.getType(), FileType.DIR, "ID 为 [{}] 的不是文件夹，不支持计算大小", id);
        // 查询当前文件夹下的所有子文件和子文件夹
        List<FileDO> children = this.fileMapper.lambdaQuery().eq(FileDO::getParentPath, dirFile.getPath()).list();
        if (CollUtil.isEmpty(children)) {
            return 0L;
        }
        // 累加子文件大小和递归计算子文件夹大小
        return children.stream().mapToLong(child -> {
            if (FileType.DIR.equals(child.getType())) {
                // 递归计算子文件夹大小
                return calcDirSize(child.getId());
            } else {
                return child.getSize();
            }
        }).sum();
    }

    /**
     * 上传文件并返回上传后的文件信息
     *
     * @param file        文件
     * @param parentPath  上级目录
     * @param storageCode 存储引擎编码
     * @param extName     文件扩展名
     * @return 文件信息
     */
    private FileInfo upload(Object file, String parentPath, String storageCode, String extName) {
        List<String> allExtensions = FileType.getAllExtensions();
        BizAssert.isTrue(!allExtensions.contains(extName), "不支持的文件类型，仅支持 {} 格式的文件", String
                .join(StringConstants.COMMA, allExtensions));
        // 构建上传预处理对象
        String platform = fileStorageService.getFileStorage().getPlatform();
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setPlatform(platform)
                .setHashCalculatorSha256(true)
                .setPath(this.pretreatmentPath(parentPath));
        // 图片文件生成缩略图
        if (FileType.IMAGE.getExtensions().contains(extName)) {
            uploadPretreatment.setIgnoreThumbnailException(true, true);
            uploadPretreatment.thumbnail(img -> img.size(100, 100));
        }
        uploadPretreatment.setProgressMonitor(new ProgressListener() {
            @Override
            public void start() {
                log.info("开始上传");
            }

            @Override
            public void progress(long progressSize, Long allSize) {
                log.info("已上传 [{}]，总大小 [{}]", progressSize, allSize);
            }

            @Override
            public void finish() {
                log.info("上传结束");
            }
        });
        // 创建父级目录
        this.createParentDir(parentPath,platform);
        // 上传
        return uploadPretreatment.upload();
    }

    /**
     * 处理路径
     *
     * <p>
     * 1.如果 path 为 {@code /}，则设置为空 <br />
     * 2.如果 path 不以 {@code /} 结尾，则添加后缀 {@code /} <br />
     * 3.如果 path 以 {@code /} 开头，则移除前缀 {@code /} <br />
     * 示例：yyyy/MM/dd/
     * </p>
     *
     * @param path 路径
     * @return 处理路径
     */
    private String pretreatmentPath(String path) {
        if (StringConstants.SLASH.equals(path)) {
            return StringConstants.EMPTY;
        }
        return StrUtil.appendIfMissing(StrUtil.removePrefix(path, StringConstants.SLASH), StringConstants.SLASH);
    }

    /**
     * 创建上级文件夹（支持多级）
     *
     * <p>
     * user/avatar/ => user（path：/user）、avatar（path：/user/avatar）
     * </p>
     *
     * @param parentPath 上级目录
     * @param platform    存储配置
     */
    @Override
    public void createParentDir(String parentPath, String platform) {
        String lockKey = StrUtil.format("Lock:{}:{}", platform, parentPath);
        try (RedisLockUtils lock = RedisLockUtils.tryLock(lockKey)) {
            if (!lock.isLocked()) {
                return; // 获取锁失败，直接返回
            }
            if (StrUtil.isBlank(parentPath) || StringConstants.SLASH.equals(parentPath)) {
                return;
            }
            // user/avatar/ => user、avatar
            String[] parentPathParts = StrUtil.split(parentPath, StringConstants.SLASH, false, true)
                .toArray(String[]::new);
            String lastPath = StringConstants.SLASH;
            StringBuilder currentPathBuilder = new StringBuilder();
            for (int i = 0; i < parentPathParts.length; i++) {
                String parentPathPart = parentPathParts[i];
                if (i > 0) {
                    lastPath = currentPathBuilder.toString();
                }
                currentPathBuilder.append(StringConstants.SLASH).append(parentPathPart);
                String currentPath = currentPathBuilder.toString();
                FileDO dirFile = this.fileMapper.lambdaQuery()
                    .eq(FileDO::getPath, currentPath)
                    .eq(FileDO::getStorageId, platform)
                    .one();
                if (dirFile != null) {
                    BizAssert.throwIfNotEqual(dirFile.getStorageId(), platform, "文件夹和上传文件存储引擎不一致");
                    continue;
                }
                FileDO file = new FileDO();
                file.setName(parentPathPart);
                file.setOriginalName(parentPathPart);
                file.setPath(currentPath);
                file.setParentPath(lastPath);
                file.setType(FileType.DIR);
                file.setStorageId(platform);
                this.fileMapper.insert(file);
            }
        }
    }

    @Override
    public PageResult<FileResp> findPage(FileQuery query, PageQuery pageQuery) {
        return null;
    }

    @Override
    public List<FileResp> list(FileQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public FileResp get(Long id) {
        return null;
    }

    @Override
    public List<Tree<Long>> tree(FileQuery query, SortQuery sortQuery, boolean b) {
        return List.of();
    }

    @Override
    public Long create(FileReq req) {
        return 0L;
    }
}