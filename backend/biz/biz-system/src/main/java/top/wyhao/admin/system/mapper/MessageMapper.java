
package top.wyhao.admin.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.admin.system.entity.SysMessage;
import top.wyhao.admin.system.model.query.MessageQuery;
import top.wyhao.admin.system.model.vo.message.MessageDetailResp;
import top.wyhao.admin.system.model.vo.message.MessageResp;
import top.wyhao.cmn.db.model.BaseMapper;

import java.util.List;

/**
 * 消息 Mapper
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
@Mapper
public interface MessageMapper extends BaseMapper<SysMessage> {

    /**
     * 分页查询消息列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 消息列表
     */
    IPage<MessageResp> selectMessagePage(@Param("page") Page<SysMessage> page, @Param("query") MessageQuery query);

    /**
     * 查询消息详情
     *
     * @param id ID
     * @return 消息详情
     */
    MessageDetailResp selectMessageById(@Param("id") Long id);

    /**
     * 查询未读消息列表
     *
     * @param userId 用户 ID
     * @return 消息列表
     */
    List<SysMessage> selectUnreadListByUserId(@Param("userId") Long userId);

    /**
     * 查询未读消息数量
     *
     * @param userId 用户 ID
     * @param type   消息类型
     * @return 未读消息数量
     */
    Long selectUnreadCountByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
}