
package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告实体
 *

 * @since 2023/8/20 10:55
 */
@Data
public class Notice{

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
    private List<String> noticeUsers;

    /**
     * 通知方式
     */
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
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}