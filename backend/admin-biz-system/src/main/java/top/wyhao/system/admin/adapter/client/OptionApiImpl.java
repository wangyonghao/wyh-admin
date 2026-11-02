package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.OptionService;
import top.wyhao.web.core.model.LabelValueResult;
import top.wyhao.web.excel.OptionApi;

import java.util.List;

/**
 * 选项查询 API 实现
 */
@Service
@RequiredArgsConstructor
public class OptionApiImpl implements OptionApi {

    private final OptionService optionService;

    @Override
    public List<LabelValueResult<String>> listByType(String type) {
        return optionService.listByType(type);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<LabelValueResult> listAll() {
        return (List) optionService.listTypes();
    }
}
