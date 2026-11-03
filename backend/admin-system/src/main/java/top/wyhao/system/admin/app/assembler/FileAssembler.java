package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.wyhao.system.admin.app.dto.FileResponse;
import top.wyhao.system.admin.app.dto.FileUploadResponse;
import top.wyhao.system.admin.domain.model.File;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;

/**
 * 文件对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface FileAssembler {

    FileResponse toResponse(File file);

    List<FileResponse> toResponseList(List<File> files);

    @Mapping(source = "id", target = "fileId")
    FileUploadResponse toUploadResponse(File file);
}
