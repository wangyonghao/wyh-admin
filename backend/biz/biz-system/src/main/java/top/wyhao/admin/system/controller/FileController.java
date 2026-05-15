package top.wyhao.admin.system.controller;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.admin.system.entity.SysFile;
import top.wyhao.admin.system.model.bo.FileRequest;
import top.wyhao.admin.system.model.query.FileQuery;
import top.wyhao.admin.system.model.vo.file.FileResult;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.starter.web.core.model.IdsRequest;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.util.HttpUtil;

import java.util.List;

/**
 * 文件管理 API
 * <p>
 * API 层只处理元数据与权限，底层路由到OSS（Object Storage Service - 文件存储服务），如 本地文件存储/MinIO/腾讯云COS/阿里云OSS/
 * </p>
 *
 * @author Yonghao Wang
 * @since 2026/5/13
 */
@Tag(name = "文件管理 API")
@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping("/system/file")
    public PageResult<FileResult> page(@Valid FileQuery query, @Valid PageQuery pageQuery) {
        return fileService.page(query, pageQuery);
    }

    @Operation(summary = "查询列表", description = "查询列表")
    @GetMapping("/system/file/list")
    public List<FileResult> list(@Valid FileQuery query) {
        List<SysFile> files = fileService.list(query);
        return BeanUtil.copyToList(files, FileResult.class);
    }

    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/system/file/{id}")
    public FileResult detail(@PathVariable Long id) {
        SysFile sysFile = fileService.detail(id);
        return toFileResult(sysFile);
    }


    @Operation(summary = "上传文件", description = "上传文件")
    @PostMapping("/system/file")
    public FileResult upload(@RequestPart @NotNull(message = "文件不能为空") MultipartFile file,
                             @RequestPart FileRequest request) {
        SysFile sysFile = fileService.upload(file, request.getPath());
        return toFileResult(sysFile);
    }

    @GetMapping("/system/file/download/{fileId}")
    public void download(@PathVariable Long fileId, HttpServletResponse response) {
        SysFile sysFile = fileService.detail(fileId);
        HttpUtil.writeAttachmentToResponse(fileService.getFileInputStream(fileId), sysFile.getFileName(), response);
    }

    @GetMapping("/system/file/preview/{fileId}")
    public void preview(@PathVariable Long fileId, HttpServletResponse response) {
        SysFile sysFile = fileService.detail(fileId);
        HttpUtil.preview(fileService.getFileInputStream(fileId), sysFile.getFileName(), sysFile.getFileType(), response);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除文件", description = "删除文件")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @DeleteMapping("/system/file/{id}")
    public void delete(@PathVariable Long id) {
        fileService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping("/system/file")
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        fileService.delete(req.getIds());
    }


    private FileResult toFileResult(SysFile sysFile) {
        return BeanUtil.copyProperties(sysFile, FileResult.class);
    }
}