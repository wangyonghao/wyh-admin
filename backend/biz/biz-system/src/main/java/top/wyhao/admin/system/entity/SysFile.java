package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件 Entity
 *

 * @since 2026/5/13
 */
@Data
@NoArgsConstructor
@TableName("sys_file")
public class SysFile {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件类型（MIME类型）
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件后缀
     */
    private String fileExtension;

    /**
     * 存储平台（local、minio、aliyun）
     */
    private String ossPlatform;

    /**
     * 文件路径
     */
    private String ossPath;

    /**
     * OSS中存储的文件名
     */
    private String ossFileName;

    /**
     * 文件URL
     */
    private String ossUrl;

    /**
     * 关联业务单号
     */
    private Long bizId;

    /**
     * 关联业务类型，如 avatar | attachment | contract
     */
    private String bizType;

    /**
     * 文件状态，
     * <ul>
     * <li>UPLOADING: 分片上传中</li>
     * <li>AVAILABLE: 可用</li>
     * <li>INFECTED: 被病毒感染</li>
     * <li>DELETED: 表示已删除</li>
     * </ul>
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
