package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单与套餐审计。只插入，不提供修改和删除。
 */
@Data
@TableName("catalog_audit_log")
public class CatalogAuditDO {

    @TableId
    private Long id;

    private String module;

    private String action;

    private String objectId;

    private String objectName;

    private String beforeValue;

    private String afterValue;

    private String result;

    private Long operatorId;

    private String operatorName;

    private String ip;

    private String userAgent;

    private LocalDateTime createTime;
}
