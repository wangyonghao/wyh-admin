
package top.wyhao.starter.web.excel;

import top.wyhao.starter.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 字典业务 API
 *

 * @since 2025/4/9 20:17
 */
public interface DictApi {

    /**
     * 根据字典类型查询
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<LabelValueResult<String>> listByDictType(String dictType);

    List<LabelValueResult> listAll();
}
