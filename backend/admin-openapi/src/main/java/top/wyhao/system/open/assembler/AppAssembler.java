package top.wyhao.system.open.assembler;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import top.wyhao.system.open.model.entity.SysApp;
import top.wyhao.system.open.model.req.AppReq;
import top.wyhao.system.open.model.resp.AppDetailResp;
import top.wyhao.system.open.model.resp.AppResult;
import top.wyhao.web.convert.BaseEnumConverters;
import top.wyhao.web.convert.MapStructConfig;

import java.util.List;

/**
 * 开放应用对象转换
 */
@Mapper(config = MapStructConfig.class, uses = BaseEnumConverters.class)
public interface AppAssembler {

    SysApp toEntity(AppReq request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(AppReq request, @MappingTarget SysApp entity);

    AppResult toResult(SysApp app);

    AppDetailResp toDetail(SysApp app);

    List<AppDetailResp> toDetailList(List<SysApp> apps);
}
