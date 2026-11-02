
package top.wyhao.system.admin.app.service;

import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.util.List;
import top.wyhao.system.admin.app.dto.MessageDetailResult;
import top.wyhao.system.admin.app.dto.MessageQuery;
import top.wyhao.system.admin.app.dto.MessageRequest;
import top.wyhao.system.admin.app.dto.MessageResult;
import top.wyhao.system.admin.app.dto.MessageUnreadResult;

/**
 * 消息业务接口
 *


 * @since 2023/10/15 19:05
 */
public interface MessageService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<MessageDetailResult> page(MessageQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    MessageResult get(Long id);

    /**
     * 将消息标记已读
     *
     * @param ids    消息ID（为空则将所有消息标记已读）
     * @param userId 用户ID
     */
    void readMessage(List<Long> ids, Long userId);

    /**
     * 查询未读消息数量
     *
     * @param userId   用户 ID
     * @param isDetail 是否查询详情
     * @return 未读消息数量
     */
    MessageUnreadResult countUnreadByUserId(Long userId, Boolean isDetail);

    /**
     * 新增
     *
     * @param req        请求参数
     * @param userIdList 接收人列表
     */
    void add(MessageRequest req, List<String> userIdList);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);
}