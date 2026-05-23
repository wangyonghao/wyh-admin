
package top.wyhao.admin.system.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.service.DictService;
import top.wyhao.starter.web.core.model.LabelValueResult;
import top.wyhao.starter.web.excel.DictApi;

import java.util.List;

/**
 * 字典业务 API 实现
 *

 * @since 2025/7/23 20:57
 */
@Service
@RequiredArgsConstructor
public class DictApiImpl implements DictApi {

    private final DictService dictService;

    @Override
    public List<LabelValueResult<String>> listByDictType(String dictType) {
        return dictService.listByDictType(dictType);
    }

    @Override
    public List<LabelValueResult> listAll() {
        return List.of(); // TODO(WYH) 待实现
    }
}
