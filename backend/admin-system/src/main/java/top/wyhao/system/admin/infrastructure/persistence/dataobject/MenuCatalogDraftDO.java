package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 某一范围（平台或租户）的菜单草稿。
 */
@Data
@TableName("menu_catalog_draft")
public class MenuCatalogDraftDO {

    @TableId(value = "scope", type = IdType.INPUT)
    private String scope;

    private Integer version;

    private Integer baseVersion;

    private String document;

    private Long updateUser;

    private LocalDateTime updateTime;
}
