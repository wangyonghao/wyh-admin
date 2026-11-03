package top.wyhao.web.excel;

import top.wyhao.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 选项查询 API
 */
public interface OptionApi {

    /**
     * 根据类型查询已启用的选项
     *
     * @param type 选项类型
     * @return 选项列表
     */
    List<LabelValueResult<String>> listByType(String type);

    /**
     * 查询全部选项类型
     */
    List<LabelValueResult> listAll();
}
