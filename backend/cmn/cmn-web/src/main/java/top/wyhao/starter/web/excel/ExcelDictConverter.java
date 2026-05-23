
package top.wyhao.starter.web.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.web.core.model.LabelValueResult;

import java.util.List;
import java.util.Objects;

/**
 * Easy Excel 字典转换器
 *

 * @since 2025/4/9 20:22
 */
public class ExcelDictConverter implements Converter<Object> {

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData,
                                    ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 获取字典数据
        List<LabelValueResult<String>> dictList = this.getDictCode(contentProperty);
        // 转换字典标签为字典值
        String value = dictList.stream()
            .filter(item -> Objects.equals(cellData.getStringValue(), item.getValue()))
            .findFirst()
            .map(LabelValueResult::getLabel)
            .orElse(null);
        // 转换字典值为对应类型
        return Convert.convert(contentProperty.getField().getType(), value);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object data,
                                                    ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (data == null) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        // 获取字典数据
        List<LabelValueResult<String>> dictList = this.getDictCode(contentProperty);
        if (CollUtil.isEmpty(dictList)) {
            return new WriteCellData<>(StringConstants.EMPTY);
        }
        // 转换字典值为字典标签
        return new WriteCellData<>(dictList.stream()
            .filter(item -> Objects.equals(data, item.getValue()))
            .findFirst()
            .map(LabelValueResult::getLabel)
            .orElse(StringConstants.EMPTY));
    }

    /**
     * 获取字典数据
     *
     * @param contentProperty Excel 内容属性
     * @return 字典数据
     */
    private List<LabelValueResult<String>> getDictCode(ExcelContentProperty contentProperty) {
        DictExcelProperty dictExcelProperty = contentProperty.getField().getAnnotation(DictExcelProperty.class);
        if (dictExcelProperty == null) {
            throw new IllegalArgumentException("Excel 字典转换器异常：请为字段添加 @DictExcelProperty 注解");
        }
        DictApi dictApi = SpringUtil.getBean(DictApi.class);
        return dictApi.listByDictType(dictExcelProperty.value());
    }
}
