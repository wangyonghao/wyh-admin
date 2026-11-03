
package top.wyhao.system.admin.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysNoticeDO;
import top.wyhao.system.admin.app.dto.DashboardNoticeResp;
import top.wyhao.mybatis.model.BaseMapper;

import java.util.List;
import top.wyhao.system.admin.app.dto.NotificationQuery;
import top.wyhao.system.admin.app.dto.NotificationResult;

/**
 * 公告 Mapper
 *

 * @since 2023/8/20 10:55
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNoticeDO> {

    /**
     * 分页查询公告列表
     *
     * @param page  分页条件
     * @param query 查询条件
     * @return 公告列表
     */
    IPage<NotificationResult> selectNoticePage(@Param("page") Page<SysNoticeDO> page, @Param("query") NotificationQuery query);

    /**
     * 查询未读公告 ID 列表
     *
     * @param noticeMethod 通知方式
     * @param userId       用户 ID
     * @return 未读公告 ID 列表
     */
    List<Long> selectUnreadIdsByUserId(@Param("noticeMethod") Integer noticeMethod, @Param("userId") Long userId);

    /**
     * 查询仪表盘公告列表
     *
     * @param userId 用户 ID
     * @return 仪表盘公告列表
     */
    List<DashboardNoticeResp> selectDashboardList(@Param("userId") Long userId);
}