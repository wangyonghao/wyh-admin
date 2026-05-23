
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.admin.system.entity.SysUserPasswordHistory;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 用户历史密码 Mapper
 *

 * @since 2024/5/16 21:58
 */
@Mapper
public interface SysUserPasswordHistoryMapper extends BaseMapper<SysUserPasswordHistory> {

    /**
     * 删除过期历史密码
     *
     * @param userId 用户 ID
     * @param count  保留 N 个历史
     */
    void deleteExpired(@Param("userId") Long userId, @Param("count") int count);
}