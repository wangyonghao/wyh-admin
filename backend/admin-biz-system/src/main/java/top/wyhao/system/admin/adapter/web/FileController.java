package top.wyhao.system.admin.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.system.admin.app.assembler.FileAssembler;
import top.wyhao.system.admin.app.dto.FileResponse;
import top.wyhao.system.admin.app.dto.FileUploadResponse;
import top.wyhao.system.admin.domain.model.File;
import top.wyhao.system.admin.app.service.FileService;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.web.core.model.IdsRequest;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.web.util.HttpUtil;

import java.io.InputStream;
import java.util.List;

/**
 * 文件 API
 *
 * <p>提供基础文件能力：上传、下载、预览、删除、分页查询。
 * 不处理业务附件、网盘等业务领域。
 *
 * @author wyh
 * @since 2026/09/16
 */
@Tag(name = "文件 API")
@RestController
@Validated
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileAssembler fileAssembler;

    @Operation(summary = "分页查询文件")
    @GetMapping("/api/files")
    public PageResult<FileResponse> page(
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @Validated PageQuery pageQuery) {
        return fileService.page(fileName, category, sortOrder, pageQuery.getPage(), pageQuery.getPageSize())
                .map(fileAssembler::toResponse);
    }

    @Operation(summary = "上传文件")
    @PostMapping("/api/files")
    public FileUploadResponse upload(
            @RequestPart @NotNull(message = "文件不能为空") MultipartFile file) {
        File uploadedFile = fileService.upload(file, LoginUtil.getUserId());
        return fileAssembler.toUploadResponse(uploadedFile);
    }

    @Operation(summary = "获取文件信息")
    @Parameter(name = "fileId", description = "文件 ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/api/files/{fileId}")
    public FileResponse getFileInfo(@PathVariable Long fileId) {
        return fileAssembler.toResponse(fileService.get(fileId));
    }

    @Operation(summary = "下载文件")
    @Parameter(name = "fileId", description = "文件 ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/api/files/{fileId}/download")
    public void download(@PathVariable Long fileId, HttpServletResponse response) {
        File file = fileService.get(fileId);
        InputStream inputStream = fileService.download(fileId);
        HttpUtil.writeAttachmentToResponse(inputStream, file.getFileName(), response);
    }

    @Operation(summary = "预览文件")
    @Parameter(name = "fileId", description = "文件 ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/api/files/{fileId}/preview")
    public void preview(@PathVariable Long fileId, HttpServletResponse response) {
        File file = fileService.get(fileId);
        InputStream inputStream = fileService.download(fileId);
        HttpUtil.preview(inputStream, file.getFileName(), response);
    }

    @Operation(summary = "删除文件")
    @Parameter(name = "fileId", description = "文件 ID", example = "1", in = ParameterIn.PATH)
    @DeleteMapping("/api/files/{fileId}")
    public void delete(@PathVariable Long fileId) {
        fileService.delete(fileId, LoginUtil.getUserId());
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/api/files")
    public void batchDelete(@RequestBody @Validated IdsRequest request) {
        List<Long> ids = request.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        fileService.delete(ids, LoginUtil.getUserId());
    }
}
