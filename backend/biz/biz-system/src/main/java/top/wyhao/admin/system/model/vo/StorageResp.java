
package top.wyhao.admin.system.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.StatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储响应参数
 *

 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储响应参数")
public class StorageResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "超级管理员")
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updateUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人", example = "李四")
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime updateTime;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "存储1")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    private String code;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private StatusEnum status;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    private String storageType;

    /**
     * Access Key
     */
    @Schema(description = "Access Key")
    private String accessKey;

    /**
     * Endpoint
     */
    @Schema(description = "Endpoint")
    private String endpoint;

    /**
     * Bucket/存储路径
     */
    @Schema(description = "Bucket/存储路径", example = "C:/tide-admin/data/file/")
    private String bucketName;

    /**
     * 域名/访问路径
     */
    @Schema(description = "域名", example = "http://localhost:8000/file")
    private String domain;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储描述")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    private Boolean isDefault;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    public Boolean getDisabled() {
        return this.getIsDefault();
    }
}