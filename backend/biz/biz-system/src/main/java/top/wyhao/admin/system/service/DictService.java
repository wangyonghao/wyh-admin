
package top.wyhao.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.wyhao.admin.system.entity.SysDict;
import top.wyhao.admin.system.model.DictQuery;
import top.wyhao.cmn.db.model.BaseService;
import top.wyhao.starter.web.core.model.LabelValueResult;
import top.wyhao.starter.web.core.model.PageQuery;

import java.util.List;

/**
 * 字典业务接口
 *

 * @since 2023/9/11 21:29
 */
public interface DictService extends BaseService<SysDict> {

    IPage<SysDict> page(DictQuery query, PageQuery pageQuery);

    /**
     * 根据字典类型查询字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<LabelValueResult<String>> listByDictType(String dictType);

    /**
     * 查询枚举字典
     *
     * @return 枚举字典列表
     */
    List<LabelValueResult<String>> listEnumDict();
}