
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import top.wyhao.system.admin.domain.model.MessageType;

import top.wyhao.system.admin.domain.model.NoticeScopes;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息实体
 *

 * @since 2023/10/15 19:05
 */
@Data
@TableName("sys_message")
public class SysMessageDO {



    /**
     * ID
     */
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
     * 类型
     */
    private MessageType type;

    /**
     * 跳转路径
     */
    private String path;

    /**
     * 通知范围
     */
    private NoticeScopes scope;

    /**
     * 通知用户
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> users;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}