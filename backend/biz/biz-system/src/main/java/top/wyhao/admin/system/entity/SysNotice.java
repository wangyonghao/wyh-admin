
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import top.wyhao.admin.system.model.enums.NoticeScopes;
import top.wyhao.admin.system.model.enums.NoticeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告实体
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Data
@TableName(value = "sys_notice", autoResultMap = true)
public class SysNotice{

    @TableId
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 分类（取值于字典 notice_type）
     */
    private String type;

    /**
     * 通知范围
     */
    private NoticeScopes noticeScope;

    /**
     * 通知用户
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> noticeUsers;

    /**
     * 通知方式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> noticeMethods;

    /**
     * 是否定时
     */
    private Boolean isTiming;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 状态
     */
    private NoticeStatus status;


    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}