
package top.wyhao.admin.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.cmn.db.model.BaseMapper;
import top.wyhao.admin.tenant.model.entity.TenantPackage;

/**
 * 套餐 Mapper
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
@Mapper
public interface PackageMapper extends BaseMapper<TenantPackage> {
}