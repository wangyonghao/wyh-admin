package top.wyhao.system.admin.app.assembler;

import org.mapstruct.Mapper;
import top.wyhao.system.admin.domain.model.Tenant;
import top.wyhao.system.admin.app.dto.TenantRequest;
import top.wyhao.tenant.client.TenantBO;
import top.wyhao.web.convert.BaseEnumConverters;
import top.wyhao.web.convert.MapStructConfig;

/**
 * 租户对象转换
 */
@Mapper(config = MapStructConfig.class, uses = BaseEnumConverters.class)
public interface TenantAssembler {

    Tenant toEntity(TenantRequest request);

    TenantBO toBO(TenantRequest request);
}
