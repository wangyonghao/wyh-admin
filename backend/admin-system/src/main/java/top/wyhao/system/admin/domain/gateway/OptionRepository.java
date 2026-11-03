package top.wyhao.system.admin.domain.gateway;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.SortableQuery;
import top.wyhao.system.admin.domain.model.Option;

import java.util.List;

/**
 * 选项仓储接口
 */
public interface OptionRepository {

    /**
     * 根据 ID 查询
     *
     * @param id ID
     * @return 选项信息
     */
    Option findById(Long id);

    /**
     * 根据 ID 列表查询
     *
     * @param ids ID 列表
     * @return 选项列表
     */
    List<Option> findByIds(List<Long> ids);

    /**
     * 插入
     *
     * @param option 选项信息
     */
    void insert(Option option);

    /**
     * 按 ID 更新
     *
     * @param option 选项信息
     */
    void updateById(Option option);

    /**
     * 批量删除
     *
     * @param ids ID 列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 分页查询
     *
     * @param query     查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<Option> page(SortableQuery query, PageParam pageParam);

    /**
     * 按类型查询已启用选项
     *
     * @param type 选项类型
     * @return 选项列表
     */
    List<Option> listEnabledByType(String type);

    /**
     * 查询全部选项类型
     *
     * @return 选项类型列表
     */
    List<String> listDistinctTypes();

    /**
     * 检查类型下值是否存在
     *
     * @param type      选项类型
     * @param value     选项值
     * @param excludeId 排除 ID
     * @return 是否存在
     */
    boolean existsValue(String type, String value, Long excludeId);

    /**
     * 统计 ID 大于指定值的数量
     *
     * @param id 阈值 ID
     * @return 数量
     */
    long countIdGreaterThan(Long id);

    /**
     * 删除 ID 大于指定值的记录
     *
     * @param id 阈值 ID
     * @return 是否删除成功
     */
    boolean deleteIdGreaterThan(Long id);
}
