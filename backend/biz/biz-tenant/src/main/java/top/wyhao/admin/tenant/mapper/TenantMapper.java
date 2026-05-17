
package top.wyhao.admin.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.tenant.model.entity.Tenant;
import top.wyhao.cmn.db.model.BaseMapper;

import java.util.List;

/**
 * 租户 Mapper
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    /**
     * 根据套餐 ID 查询数量
     *
     * @param packageIds 套餐 ID 列表
     * @return 租户数量
     */
    default Long countByPackageIds(List<Long> packageIds) {
        return this.lambdaQuery().in(Tenant::getPackageId, packageIds).count();
    }
}