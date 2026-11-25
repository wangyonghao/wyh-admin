package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业当前生效的套餐。主套餐和增购包各一行。
 */
@Data
@TableName("tenant_package_grant")
public class TenantPackageGrantDO {

    @TableId(value = "tenant_id", type = IdType.INPUT)
    private Long tenantId;

    private Long packageId;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
