package top.wyhao.system.admin.app.service;

import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.OptionQuery;
import top.wyhao.system.admin.app.dto.OptionRequest;
import top.wyhao.system.admin.app.dto.OptionResult;
import top.wyhao.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 选项业务接口
 */
public interface OptionService {

    /**
     * 分页查询
     */
    PageResult<OptionResult> page(OptionQuery query, PageParam pageParam);

    /**
     * 查询详情
     */
    OptionResult detail(Long id);

    /**
     * 新增
     *
     * @return ID
     */
    Long create(OptionRequest request);

    /**
     * 修改
     */
    void update(Long id, OptionRequest request);

    /**
     * 批量删除
     */
    void delete(List<Long> ids);

    /**
     * 清除指定类型的缓存
     */
    void clearCache(String type);

    /**
     * 按类型查询已启用的选项
     */
    List<LabelValueResult<String>> listByType(String type);

    /**
     * 查询全部选项类型
     */
    List<LabelValueResult<String>> listTypes();
}
