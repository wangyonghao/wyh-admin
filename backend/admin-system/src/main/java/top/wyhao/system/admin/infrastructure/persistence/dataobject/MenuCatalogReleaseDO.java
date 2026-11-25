package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单发布快照。
 */
@Data
@TableName("menu_catalog_release")
public class MenuCatalogReleaseDO {

    @TableId
    private Long id;

    private String scope;

    private Integer version;

    private String document;

    private Long createUser;

    private LocalDateTime createTime;
}
