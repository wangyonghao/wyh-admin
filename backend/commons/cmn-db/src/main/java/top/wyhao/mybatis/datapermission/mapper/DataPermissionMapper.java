
package top.wyhao.mybatis.datapermission.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.mybatis.datapermission.annotation.DataScope;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限 Mapper 基类
 *
 * @param <T> 实体类
 */
public interface DataPermissionMapper<T> extends BaseMapper<T> {

    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return 全部记录
     */
    @DataScope
    @Override
    List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 entity 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     * @return 全部记录（并翻页）
     */
    @DataScope
    @Override
    List<T> selectList(IPage<T> page, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 ID 删除
     *
     * @param id id
     * @return 删除个数
     */
    @DataScope
    @Override
    int deleteById(@Param("id") Serializable id);
}
