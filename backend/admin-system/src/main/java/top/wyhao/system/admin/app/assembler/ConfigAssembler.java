package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import top.wyhao.system.admin.app.dto.ConfigRequest;
import top.wyhao.system.admin.app.dto.ConfigResult;
import top.wyhao.system.admin.domain.model.Config;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;

/**
 * 系统配置对象转换
 */
@Mapper(config = MapStructConfig.class)
public interface ConfigAssembler {

    @Mapping(source = "createTime", target = "createdAt")
    @Mapping(source = "updateTime", target = "updatedAt")
    @Mapping(target = "version", ignore = true)
    ConfigResult toResult(Config config);

    List<ConfigResult> toResultList(List<Config> configs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Config toEntity(ConfigRequest request);
}
