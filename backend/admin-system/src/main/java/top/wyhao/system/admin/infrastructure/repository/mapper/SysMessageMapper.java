
package top.wyhao.system.admin.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMessageDO;
import top.wyhao.mybatis.model.BaseMapper;

import java.util.List;
import top.wyhao.system.admin.app.dto.MessageDetailResult;
import top.wyhao.system.admin.app.dto.MessageQuery;
import top.wyhao.system.admin.app.dto.MessageResult;

/**
 * 消息 Mapper
 *

 * @since 2023/10/15 19:05
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessageDO> {

    /**
     * 分页查询消息列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 消息列表
     */
    IPage<MessageDetailResult> selectMessagePage(@Param("page") Page<SysMessageDO> page, @Param("query") MessageQuery query);

    /**
     * 查询消息详情
     *
     * @param id ID
     * @return 消息详情
     */
    MessageResult selectMessageById(@Param("id") Long id);

    /**
     * 查询未读消息列表
     *
     * @param userId 用户 ID
     * @return 消息列表
     */
    List<SysMessageDO> selectUnreadListByUserId(@Param("userId") Long userId);

    /**
     * 查询未读消息数量
     *
     * @param userId 用户 ID
     * @param type   消息类型
     * @return 未读消息数量
     */
    Long selectUnreadCountByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
}